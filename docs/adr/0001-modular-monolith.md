# ADR 0001 — Modular Monolith em vez de Microsserviços

## Status

Aceito

## Contexto

O sistema precisa suportar múltiplos domínios de negócio fortemente relacionados —
identidade, estabelecimentos, funcionários, catálogo de serviços, agendamento,
notificações, relatórios e auditoria — para um produto SaaS ainda em fase de
validação de mercado (MVP Enterprise).

Arquiteturas de microsserviços são frequentemente adotadas prematuramente, antes que
os limites reais de domínio (bounded contexts) estejam suficientemente maduros e
estáveis. Dividir o sistema em serviços distribuídos cedo demais tende a gerar:

- Fronteiras de domínio erradas, difíceis e caras de corrigir depois (ao contrário de
  um monolito modular, onde mover uma classe entre pacotes é uma refatoração local).
- Complexidade operacional desproporcional ao estágio do produto: múltiplos deploys,
  service discovery, tracing distribuído, contratos de API entre serviços, gestão de
  consistência eventual em toda comunicação entre domínios — mesmo para operações que
  hoje são simples chamadas de método.
- Overhead de infraestrutura (múltiplos bancos, múltiplos pipelines de CI/CD, múltiplos
  ambientes de observabilidade) sem tráfego real que justifique escalabilidade
  independente por serviço.
- Transações que hoje são atômicas (ex.: criar `Employee` e seu `User` de identidade
  juntos) se tornariam transações distribuídas, exigindo Sagas ou processos de
  compensação — complexidade real, não incidental.

Por outro lado, um monolito tradicional (não modular) — onde qualquer classe pode
depender de qualquer outra sem fronteiras — acumula acoplamento implícito que se torna
igualmente caro de desfazer no futuro, caso a extração de um domínio para um serviço
independente se torne necessária (por motivo de escala, time dedicado, ou requisito de
isolamento regulatório).

## Decisão

Adotar um **Modular Monolith**: uma única aplicação deployável, mas internamente
organizada em módulos de negócio com fronteiras de domínio explícitas e baixo
acoplamento (`identity`, `business`, `employee`, `customer`, `catalog`, `scheduling`,
`notification`, `reporting`, `audit`, `shared`).

Cada módulo segue internamente Arquitetura Hexagonal (`domain` → `application` →
`infrastructure` → `interfaces`), e a comunicação entre módulos ocorre through:

- **Ports explícitos** quando um módulo precisa de uma capacidade de outro sem
  depender da sua implementação concreta (ex.: `BusinessContextResolver`,
  `EmployeeNameResolver`, `AuditRecorder`).
- **Domain Events via Transactional Outbox** (ver ADR 0002) para efeitos colaterais
  assíncronos entre módulos (ex.: `scheduling` publica `AppointmentCreated`;
  `notification` e `reporting` reagem).
- Em poucos casos deliberadamente documentados, dependência direta entre módulos
  quando a operação é intrinsecamente uma transação única que atravessa dois
  contextos (ex.: `CreateEmployeeUseCase` cria tanto o `User` de identidade quanto o
  `Employee` na mesma transação — documentado explicitamente no código como exceção
  consciente).

As fronteiras de módulo são verificadas automaticamente por testes ArchUnit (domínio
nunca depende de infraestrutura, controllers nunca acessam repositories diretamente),
tornando o acoplamento acidental um erro de build, não uma questão de disciplina de
code review.

## Alternativas Consideradas

### Microsserviços desde o início

Rejeitada. O custo operacional (múltiplos deploys, tracing distribuído, contratos de
API versionados entre serviços, consistência eventual generalizada) não se justifica
antes que o produto tenha tração real e limites de domínio validados em produção. A
Seção 149 do escopo original explicitamente proíbe "criar microsserviços sem
necessidade".

### Monolito sem modularização

Rejeitada. Sem fronteiras de módulo explícitas e verificadas, o acoplamento entre
domínios cresce organicamente e silenciosamente — um `ServiceController` importando
diretamente um `EmployeeJpaRepository`, por exemplo, não geraria nenhum erro de
compilação, apenas uma dívida técnica invisível até se tornar cara demais para
desfazer.

## Consequências

**Positivas**

- Deploy único, mais simples de operar, testar e depurar nesta fase do produto.
- Transações ACID reais entre módulos que precisam de atomicidade (ex.: criação de
  `Employee` + `User`), sem necessidade de Sagas ou compensação.
- Refatoração de fronteiras de domínio é uma operação local (mover classes entre
  pacotes), não uma mudança de contrato de rede entre serviços.
- Testes de integração podem validar fluxos completos ponta a ponta (HTTP → domínio →
  banco → outbox → RabbitMQ → consumer) em um único processo de teste, com
  Testcontainers reais — sem precisar orquestrar múltiplos serviços.

**Negativas / trade-offs aceitos**

- Escalabilidade horizontal é da aplicação inteira, não por módulo — se `scheduling`
  precisar de 10x mais capacidade que `reporting`, ambos escalam juntos. Aceitável no
  estágio atual; seria revisado se um módulo específico se tornasse um gargalo real de
  produção.
- Qualquer módulo pode, em teoria, ser comprometido por um bug em outro módulo dentro
  do mesmo processo (falta de isolamento de falha entre domínios). Mitigado
  parcialmente por circuit breakers futuros nas integrações externas (Seção 113) e
  pela separação de banco de dados por tenant lógico (não por módulo).
- Requer disciplina de engenharia contínua para não deixar o acoplamento entre módulos
  crescer — mitigado pelos testes ArchUnit, mas ainda depende de manutenção dessas
  regras à medida que o sistema evolui.

**Caminho de evolução**

Se e quando um módulo específico precisar ser extraído (ex.: `notification` como
serviço dedicado por motivo de escala ou de equipe), a fronteira já modular e a
comunicação já baseada em eventos de domínio tornam essa extração uma mudança de
infraestrutura de deploy, não uma reescrita de lógica de negócio.
