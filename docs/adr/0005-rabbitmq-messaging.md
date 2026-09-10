# ADR 0005 — RabbitMQ como Broker de Mensageria

## Status

Aceito

## Contexto

Diversos efeitos colaterais de operações de negócio precisam ser processados de forma
assíncrona e desacoplada do caminho crítico da requisição HTTP:

- Notificações de confirmação/cancelamento de agendamento ao cliente.
- Lembretes automáticos (24h e 2h antes do horário marcado).
- Geração de relatórios, que pode envolver processamento pesado sobre grandes volumes
  de agendamentos e não deve bloquear a requisição `POST /reports` (Seção 35 do
  escopo original é explícita sobre isso).

Esses processamentos precisam de garantias que uma simples chamada assíncrona em
memória (ex.: `@Async` do Spring) não oferece:

- **Durabilidade**: um evento não pode ser perdido se a aplicação reiniciar ou cair
  entre a criação do evento e seu processamento.
- **Retry com backoff**: falhas transitórias (um provedor de notificação
  temporariamente indisponível, por exemplo) devem ser tentadas novamente
  automaticamente, sem intervenção manual, mas sem sobrecarregar o sistema com
  retries imediatos.
- **Dead Letter Queue**: falhas permanentes (mensagens malformadas, bugs de
  processamento) precisam ser isoladas para investigação, sem bloquear o
  processamento de mensagens subsequentes na mesma fila.
- **Múltiplos consumidores independentes** por tipo de evento (ex.: um agendamento
  criado pode interessar tanto ao módulo de notificação quanto, futuramente, a um
  módulo de analytics) sem acoplar o publicador a saber quem consome.

## Decisão

Adotar **RabbitMQ** como broker de mensageria, com a seguinte topologia:

- **Exchanges do tipo topic/direct** por domínio de evento: `appointment.events`,
  `notification.events`, `report.events` — permitindo roteamento flexível por
  routing key (ex.: `appointment.created`, `appointment.cancelled`) sem acoplar
  publicador e consumidor a uma fila específica.
- **Uma fila principal por caso de uso de consumo**: `appointment.notification.queue`,
  `appointment.reminder.queue`, `report.generation.queue` — cada uma com seu próprio
  consumer dedicado.
- **Cadeia de retry por fila**, usando uma fila de retry dedicada (sem consumidores) e
  TTL configurado dinamicamente por mensagem (via a propriedade `expiration`),
  permitindo backoff exponencial (5s → 30s → 5min, mesma progressão usada pelo
  `OutboxPublisherJob` — Seção 32) sem precisar de uma fila fixa por tier de delay.
- **Dead Letter Queue final** por fila, para onde mensagens são roteadas
  explicitamente após esgotar o número máximo de tentativas — nunca de forma
  automática via requeue nativo do RabbitMQ, o que daria menos controle sobre o TTL
  de cada tentativa.
- **Acknowledgment manual** em todos os consumers: uma mensagem só é confirmada
  (`basicAck`) depois que seu efeito de negócio é processado com sucesso (ou
  explicitamente roteada para retry/DLQ em caso de falha) — nunca com auto-ack, que
  arriscaria perder mensagens em caso de falha entre o recebimento e o processamento.
- **Deduplicação persistente do lado do consumidor** (`processed_events`, chave
  composta `eventId + consumer`), complementando a garantia de "at-least-once
  delivery" do RabbitMQ com idempotência de efeito — uma mensagem redelivered nunca
  produz um efeito duplicado.

A publicação de eventos nunca ocorre diretamente do código de negócio: todo evento
passa primeiro pelo Transactional Outbox (ADR 0002), e é o `OutboxPublisherJob` quem
efetivamente publica no RabbitMQ através do port `MessageBroker` — mantendo o domínio
e a aplicação completamente desacoplados de RabbitMQ como tecnologia concreta.

## Alternativas Consideradas

### Apache Kafka

Considerado, mas rejeitado para o estágio atual do produto. Kafka é otimizado para
alto throughput de streaming e retenção de longo prazo de um log de eventos
imutável — características valiosas para arquiteturas orientadas a eventos em maior
escala, mas que introduzem complexidade operacional (particionamento, consumer
groups, compactação de tópicos) desproporcional ao volume de eventos deste sistema no
MVP. RabbitMQ, com seu modelo de filas com reconhecimento (acknowledgment) e roteamento
flexível via exchanges, é mais direto para o padrão de "processar um evento e
confirmar" que domina os casos de uso atuais (notificação, lembrete, relatório).

### `@Async` do Spring com um `ThreadPoolTaskExecutor`

Rejeitada como mecanismo primário. Processamento assíncrono em memória não sobrevive a
um restart da aplicação — um evento "em voo" no momento de um deploy ou crash seria
simplesmente perdido, sem qualquer garantia de durabilidade ou possibilidade de retry.
Inaceitável para notificações e relatórios, que são funcionalidades visíveis ao
usuário final.

### AWS SQS / Google Cloud Pub/Sub (mensageria gerenciada)

Rejeitada para o MVP por acoplar a aplicação a um provedor de nuvem específico antes
de uma decisão de infraestrutura de deploy ter sido tomada (o README documenta
explicitamente que o CD não aponta ainda para uma infraestrutura de destino
específica). RabbitMQ auto-hospedado (via Docker Compose neste estágio) mantém a opção
de portabilidade entre provedores em aberto.

## Consequências

**Positivas**

- Garantias de entrega (at-least-once), retry automático com backoff, e DLQ são
  fornecidos pela infraestrutura de mensageria, não reimplementados em cada consumer —
  o padrão de retry/dedup é centralizado em `RetryingMessageProcessor` e
  `EventDeduplicationService`, reutilizado por todos os consumers do sistema.
- Desacoplamento real entre publicador e consumidor: `scheduling` publica
  `AppointmentCreated` sem saber que `notification` e (potencialmente, no futuro)
  outros módulos o consomem — novos consumidores podem ser adicionados sem alterar o
  código de `scheduling`.
- RabbitMQ Management UI (exposta via Docker Compose) oferece visibilidade operacional
  imediata sobre filas, taxas de consumo e mensagens presas em DLQ, sem ferramental
  adicional.
- Testado com broker real via Testcontainers (não mockado) em toda a suíte de
  integração relacionada a mensageria — incluindo um teste de ponta a ponta que prova
  que uma mensagem que falha permanentemente de fato chega na DLQ.

**Negativas / trade-offs aceitos**

- Introduz uma dependência de infraestrutura adicional que precisa ser operada,
  monitorada e mantida disponível em produção — mitigado por health checks
  automáticos do Actuator sobre a conectividade RabbitMQ, e pelo `OutboxHealthIndicator`
  que detecta indiretamente indisponibilidade prolongada via eventos `PENDING`
  acumulados.
- O retry via TTL por mensagem (em vez do requeue nativo do RabbitMQ) exige uma
  fila de retry dedicada por fila principal, aumentando o número de filas declaradas
  na topologia — uma complexidade de configuração aceita em troca do controle preciso
  sobre o tempo de espera de cada tentativa.
- `@Scheduled` para o `OutboxPublisherJob` roda em todas as instâncias da aplicação
  simultaneamente por design (não usa lock distribuído como ShedLock) — seguro apenas
  porque a exclusividade real é garantida por `SELECT ... FOR UPDATE SKIP LOCKED` no
  nível do banco (ver ADR 0002), não pela coordenação do scheduler em si; essa
  dependência entre as duas decisões precisa ser mantida em mente caso o mecanismo de
  locking do outbox seja alterado no futuro.
