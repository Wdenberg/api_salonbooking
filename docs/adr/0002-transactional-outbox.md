# ADR 0002 — Transactional Outbox Pattern

## Status

Aceito

## Contexto

Diversas operações de negócio precisam, além de persistir uma mudança de estado no
PostgreSQL, notificar outras partes do sistema de forma assíncrona: criar um
agendamento deve eventualmente disparar uma notificação ao cliente; confirmar,
cancelar ou completar um agendamento também; solicitar um relatório deve disparar seu
processamento em background.

A forma ingênua de implementar isso seria publicar diretamente no RabbitMQ dentro do
mesmo método que persiste a mudança no banco:

```
BEGIN TRANSACTION
  INSERT INTO appointments (...)
  rabbitTemplate.convertAndSend(...)   // chamada de rede para um sistema externo
COMMIT
```

Esse padrão tem uma falha estrutural: **não existe transação distribuída real entre
PostgreSQL e RabbitMQ**. Isso produz dois cenários de inconsistência:

1. O commit no banco é bem-sucedido, mas a publicação no RabbitMQ falha (rede
   instável, broker temporariamente indisponível, timeout). O agendamento existe no
   banco, mas nenhuma notificação jamais será enviada — o evento foi perdido
   silenciosamente.
2. Se a chamada ao RabbitMQ ocorrer *antes* do commit e o commit falhar depois (por
   qualquer motivo — violação de constraint, deadlock, etc.), o evento já foi
   publicado para um agendamento que nunca existiu de fato no banco.

Como o sistema depende de RabbitMQ para funcionalidades essenciais — notificações,
lembretes, geração de relatórios — perder eventos silenciosamente é inaceitável, e a
frequência desses cenários aumenta proporcionalmente à carga e à instabilidade de rede
em produção, não é um caso extremo teórico.

## Decisão

Implementar o **Transactional Outbox Pattern**: toda mudança de estado que precisa
gerar um efeito assíncrono grava, **na mesma transação de banco de dados** da
operação de negócio, uma linha na tabela `outbox_events`:

```
BEGIN TRANSACTION
  INSERT INTO appointments (...)
  INSERT INTO outbox_events (event_type='AppointmentCreated', payload=..., status='PENDING')
COMMIT
```

Como ambas as escritas fazem parte da mesma transação PostgreSQL, elas são atômicas
por construção: ou as duas persistem, ou nenhuma persiste. Não há mais dependência de
uma chamada de rede síncrona bem-sucedida dentro do caminho crítico da requisição.

Um processo separado — `OutboxPublisherJob`, agendado via `@Scheduled` — consulta
periodicamente eventos `PENDING` e os publica no RabbitMQ, marcando-os como
`PUBLISHED` após confirmação, ou reagendando com backoff exponencial em caso de falha
(até um limite de tentativas, após o qual o evento é marcado `FAILED` para
investigação manual).

A concorrência entre múltiplas instâncias do publisher (necessária para suportar
múltiplas réplicas da aplicação — Seção 110/111 do escopo original) é resolvida com
`SELECT ... FOR UPDATE SKIP LOCKED`: cada instância do publisher processa um lote de
eventos disjunto dos demais, sem bloqueio mútuo e sem necessidade de coordenação
distribuída (ex.: ShedLock) — a garantia de exclusividade vive inteiramente na
constraint de locking do próprio PostgreSQL.

A publicação (port `DomainEventPublisher`, implementado por
`OutboxDomainEventPublisherAdapter`) é injetada nos use cases como uma dependência de
aplicação comum, sem qualquer conhecimento de RabbitMQ — o `MessageBroker` concreto
(inicialmente um stub de log, depois RabbitMQ real) só é acionado pelo
`OutboxPublisherJob`, nunca diretamente pelo código de negócio.

## Alternativas Consideradas

### Publicar diretamente no RabbitMQ dentro da transação de negócio

Rejeitada pelos motivos descritos no Contexto — gera inconsistência garantida sob
falha parcial, que se torna mais provável, não menos, à medida que o sistema escala.

### Change Data Capture (CDC) via Debezium lendo o WAL do PostgreSQL

Considerada como evolução futura possível, mas rejeitada para o MVP por adicionar uma
peça de infraestrutura adicional (Kafka Connect ou equivalente) sem necessidade
comprovada no estágio atual do produto — YAGNI. A tabela `outbox_events` foi desenhada
de forma compatível com uma futura migração para CDC caso o volume de eventos
justifique eliminar o polling do `OutboxPublisherJob`.

### Publicar de forma "best effort" e aceitar perda ocasional de eventos

Rejeitada. Notificações perdidas e relatórios nunca processados são falhas visíveis e
inaceitáveis para o usuário final, não um detalhe de implementação tolerável.

## Consequências

**Positivas**

- Consistência garantida entre o estado de negócio e os eventos publicados — nenhuma
  falha de rede no RabbitMQ pode causar perda silenciosa de um evento já commitado.
- Retry e Dead Letter Queue tornam-se responsabilidade de uma única peça de
  infraestrutura (`OutboxPublisherJob` + `RetryingMessageProcessor`), reutilizável por
  todos os módulos que publicam eventos, em vez de reimplementados em cada caso de uso.
- Múltiplas instâncias da aplicação podem rodar o publisher concorrentemente sem
  qualquer coordenação distribuída adicional, graças a `FOR UPDATE SKIP LOCKED`.
- Trocar a implementação de `MessageBroker` (de um stub de log para RabbitMQ real, ou
  futuramente para outro broker) é uma mudança de um único bean, sem tocar em nenhum
  use case ou no próprio `OutboxPublisherJob`.

**Negativas / trade-offs aceitos**

- Latência adicional entre a operação de negócio e a entrega efetiva do evento —
  limitada pelo intervalo de polling do `OutboxPublisherJob` (configurável,
  tipicamente poucos segundos), não é entrega instantânea.
- Tabela `outbox_events` cresce continuamente e precisa de uma estratégia de limpeza
  (arquivamento ou purga de eventos `PUBLISHED` antigos) — não implementada no MVP,
  documentada como item de manutenção operacional futura.
- Introduz um componente de infraestrutura adicional (o job de publicação) que precisa
  de seu próprio monitoramento — endereçado pelo `OutboxHealthIndicator` (Fase 14), que
  reporta `DOWN` quando eventos `PENDING` ficam presos além de um limiar de tempo.
