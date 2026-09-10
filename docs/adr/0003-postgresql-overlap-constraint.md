# ADR 0003 — PostgreSQL Exclusion Constraint contra Double Booking

## Status

Aceito

## Contexto

A regra de negócio mais crítica do sistema é: dois agendamentos ativos (`PENDING` ou
`CONFIRMED`) nunca podem se sobrepor no tempo para o mesmo funcionário. Essa é
literalmente a promessa central de um sistema de agendamento — violá-la significa
vender o mesmo horário duas vezes.

A abordagem ingênua de verificar disponibilidade antes de inserir é estruturalmente
insegura sob concorrência:

```
SELECT COUNT(*) FROM appointments
WHERE employee_id = ? AND status IN ('PENDING','CONFIRMED')
  AND start_at < ? AND end_at > ?
-- se count = 0:
INSERT INTO appointments (...)
```

Entre o `SELECT` e o `INSERT` existe uma janela de tempo em que duas requisições
concorrentes podem ambas observar "nenhum conflito" e ambas prosseguirem para o
`INSERT`, resultando em dois agendamentos sobrepostos persistidos. Isso não é um caso
extremo teórico: é o comportamento esperado sempre que dois clientes tentam reservar o
mesmo horário popular ao mesmo tempo — exatamente o cenário que mais importa evitar.

Soluções de aplicação para essa classe de problema (locks pessimistas em nível de
aplicação, locks otimistas com retry, filas de serialização por funcionário) existem,
mas todas dependem de que **toda** instância da aplicação, em **todo** momento,
respeite corretamente o mecanismo de coordenação — e falham silenciosamente se alguma
via de escrita (uma migração de dados, um script administrativo, uma futura
funcionalidade) contornar essa camada.

## Decisão

Delegar a garantia final de não-sobreposição ao próprio banco de dados, usando uma
**Exclusion Constraint** do PostgreSQL sobre a tabela `appointments`:

```sql
ALTER TABLE appointments
    ADD CONSTRAINT excl_appointments_employee_overlap
    EXCLUDE USING gist (
        employee_id WITH =,
        tstzrange(start_at, end_at) WITH &&
    )
    WHERE (status IN ('PENDING', 'CONFIRMED'));
```

Essa constraint usa um índice GiST (requerendo a extensão `btree_gist`, já que
`employee_id` é um valor de igualdade simples combinado com um operador de intervalo)
para impedir, a nível de storage engine, que duas linhas com o mesmo `employee_id` e
intervalos de tempo sobrepostos coexistam — desde que ambas estejam em um status
"ativo" (`PENDING` ou `CONFIRMED`; agendamentos cancelados, concluídos ou marcados como
no-show são excluídos da constraint via a cláusula `WHERE`, permitindo que um horário
liberado por cancelamento seja reutilizado).

A aplicação mantém duas camadas adicionais, mas nenhuma delas é a garantia final:

1. **Validação de regras de negócio** em `CreateAppointmentUseCase` (horário futuro,
   dentro do expediente, dentro da escala do funcionário, sem bloqueios) — falha
   rápido com mensagens claras para os casos comuns e válidos.
2. **Pré-check em memória** (`existsOverlapping`) — evita a maioria das tentativas de
   conflito de chegarem ao banco, melhorando a experiência do usuário com um erro mais
   imediato, mas **não é a fonte de verdade**.
3. **A constraint do banco** — a única garantia que efetivamente não pode ser
   contornada por uma race condition, um bug de aplicação, ou uma via de escrita
   alternativa.

Quando a constraint é violada, o PostgreSQL levanta uma exceção
`exclusion_violation` (SQLState `23P01`), que o `AppointmentRepositoryAdapter` traduz
para `AppointmentConflictException` → `HTTP 409 APPOINTMENT_CONFLICT`. O `save()` usa
`saveAndFlush()` deliberadamente, para que a violação seja detectada de forma síncrona
dentro do método, e não apenas no commit da transação Spring — momento em que seria
tarde demais para converter a falha em uma resposta HTTP limpa.

## Alternativas Consideradas

### Lock pessimista em nível de aplicação (`SELECT ... FOR UPDATE` em uma linha de "slot")

Rejeitada como mecanismo único. Exigiria modelar um "slot" reservável como uma
entidade separada, adicionando complexidade ao domínio sem eliminar o risco de um
caminho de escrita que não passe pelo lock.

### Lock otimista (`@Version`) no `Appointment`

Não aplicável diretamente ao problema: `@Version` protege contra atualizações
concorrentes da *mesma linha*, mas o conflito de double booking ocorre entre linhas
*diferentes* (dois `INSERT`s distintos que deveriam ser mutuamente exclusivos) — um
problema estrutural diferente do que optimistic locking resolve.

### Fila de serialização por funcionário (processar todas as reservas de um funcionário sequencialmente via uma fila RabbitMQ dedicada)

Rejeitada para o MVP. Introduziria latência artificial em todas as reservas (mesmo as
que nunca colidiriam), acoplaria a disponibilidade da API de agendamento à
disponibilidade do RabbitMQ para uma operação que deveria poder ser validada
diretamente contra o banco, e ainda assim exigiria uma garantia de unicidade no nível
de armazenamento como rede de segurança.

### Confiar apenas na validação de aplicação, sem proteção de banco

Rejeitada explicitamente pelo escopo original do projeto (Seção 149: "confiar apenas
em SELECT para evitar double booking" está na lista do que não fazer). Validado na
prática por um teste de concorrência real com 10 requisições HTTP simultâneas para o
mesmo horário — sem a constraint, esse teste falharia de forma não determinística.

## Consequências

**Positivas**

- Garantia absoluta de não-sobreposição, independente de bugs de aplicação, número de
  instâncias da aplicação rodando simultaneamente, ou vias de escrita futuras que
  eventualmente contornem a camada de validação em Java.
- Nenhuma coordenação distribuída (locks, filas de serialização) é necessária para
  múltiplas instâncias da aplicação escalarem horizontalmente — a exclusividade é uma
  propriedade do dado no banco, não do processo que o escreve.
- Comportamento validado empiricamente por teste de concorrência real, não apenas por
  inspeção de código.

**Negativas / trade-offs aceitos**

- Acopla o schema do banco a uma extensão específica do PostgreSQL (`btree_gist`),
  reduzindo a portabilidade teórica para outro SGBD — aceito, já que o projeto já
  assume PostgreSQL como decisão de stack (Seção 5 do escopo original) e a
  funcionalidade de exclusion constraints é um recurso maduro e estável do PostgreSQL,
  não experimental.
- O erro de conflito só é detectado no momento do `INSERT`/`flush`, exigindo tratamento
  explícito de exceção na camada de persistência (`AppointmentRepositoryAdapter`) para
  traduzir um erro de baixo nível do driver JDBC em uma exceção de domínio compreensível
  — complexidade adicional, mas contida em um único ponto do código.
- `EXCLUDE USING gist` tem custo de manutenção de índice ligeiramente maior que um
  índice B-tree simples em operações de escrita — aceitável dado o volume esperado de
  agendamentos por funcionário/dia, e revisável caso o perfil de carga mude
  substancialmente.
