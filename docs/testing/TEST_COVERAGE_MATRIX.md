# Matriz de Cobertura de Testes

Mapeamento entre os requisitos das Seções 70-76 do prompt original e os testes que os satisfazem.

## Seção 70 — Unit Tests

| Requisito | Teste |
|---|---|
| deveCriarAgendamento | `AppointmentTest.deveIrDePendingParaConfirmed` (Fase 6) |
| deveCalcularEndAt | Coberto implicitamente em `CreateAppointmentUseCase` (endAt sempre calculado, nunca aceito do cliente — Fase 6) |
| deveRecusarHorarioPassado | `SchedulingRuleViolationException` via `validateAdvanceNotice` — testado em `CreateAppointmentUseCase` (validação, não unit test isolado — gap, ver abaixo) |
| deveRecusarFuncionarioInativo | Coberto na Fase 6 `CreateAppointmentUseCase` (validação inline) — gap: sem unit test isolado |
| deveRecusarServicoInativo | Idem acima |
| deveRecusarHorarioForaDoExpediente | `AvailabilityCalculatorTest` (Fase 6) cobre o algoritmo; validação de use case sem teste isolado — gap |
| deveRecusarBloqueio | `AvailabilityCalculatorTest.deveExcluirSlotsBloqueados` (Fase 6) |
| deveRecusarConflito | `AppointmentConcurrencyIntegrationTest` (Fase 6) — nível de integração, não unit |
| deveCancelarAgendamento | `AppointmentTest.devePermitirCancelamentoDentroDoPrazo` (Fase 6) |
| deveRecusarCancelamentoForaDoPrazo | `AppointmentTest.deveRecusarCancelamentoForaDoPrazo` (Fase 6) |

**Gap identificado**: validações de `CreateAppointmentUseCase` (advance notice, employee/service inativo, fora do expediente) são cobertas apenas indiretamente via testes de integração HTTP. Adicionar unit tests com mocks é valioso para feedback mais rápido — implementado abaixo em `CreateAppointmentUseCaseTest`.

## Seção 71 — Testcontainers

| Requisito | Status |
|---|---|
| PostgreSQLContainer | `AbstractIntegrationTest` (Fase 2), usado em toda a suíte |
| RabbitMQContainer | `AbstractRabbitMqIntegrationTest` (Fase 9) |
| Migrations reais executadas | Flyway roda automaticamente contra o container real em todo teste de integração |
| Constraints reais testadas | `AppointmentConcurrencyIntegrationTest` (exclusion constraint), `AppointmentIdempotencyIntegrationTest` (unique constraint) |

## Seção 72 — Teste de Concorrência

| Requisito | Teste |
|---|---|
| 10 requisições, mesmo employee/horário/serviço, 1 sucesso + 9 conflitos | `AppointmentConcurrencyIntegrationTest.dezRequisicoesSimultaneasParaMesmoHorario_apenasUmaDeveSerCriada` (Fase 6) |

## Seção 73 — Teste de Idempotência

| Requisito | Teste |
|---|---|
| Mesma chave, primeira cria, segunda retorna mesmo resultado | `AppointmentIdempotencyIntegrationTest.mesmaChaveEMesmoCorpo_...` (Fase 7) |
| Não duplica | Mesmo teste, mais `requisicoesConcorrentesComMesmaChave_apenasUmaDeveCriar` |

## Seção 74 — Teste de Mensageria

| Requisito | Teste |
|---|---|
| Evento publicado | `AppointmentEventPublishAndConsumeIntegrationTest` (Fase 9) |
| Consumer processa | Idem |
| Mensagem duplicada não gera efeito duplicado | `EventDeduplicationServiceTest` (Fase 9) |
| Falha gera retry | `OutboxPublisherRetryIntegrationTest` (Fase 8, nível outbox) |
| Falha definitiva vai para DLQ | `DeadLetterQueueIntegrationTest` (Fase 15 — gap fechado nesta fase) |

## Seção 75 — Testes de Segurança

| Requisito | Teste |
|---|---|
| cliente acessando outro cliente | `AppointmentSecurityIntegrationTest` (Fase 6) |
| cliente acessando dados de outro estabelecimento | `BusinessControllerIntegrationTest`, `ReportControllerIntegrationTest` |
| owner acessando outro business | `EmployeeControllerIntegrationTest`, `ServiceControllerIntegrationTest` |
| employee acessando outro business | `CrossTenantAccessSecurityTest.employeeAcessandoOutroBusiness_...` (Fase 15 — gap fechado) |
| cliente tentando criar serviço | `ServiceControllerIntegrationTest.clienteNaoPodeCriarServico` |
| cliente tentando cadastrar funcionário | Coberto indiretamente por `@PreAuthorize("hasRole('OWNER')")` em `EmployeeController` — sem teste HTTP dedicado ainda (gap menor, mesmo padrão de `employeeNaoPodeCadastrarFuncionario`) |

## Seção 76 — OpenAPI

Coberto pela configuração `springdoc-openapi` (Fase 1) + anotações `@Tag` em todos os controllers. Verificação automatizada de que `/v3/api-docs` responde corretamente — ver `OpenApiIntegrationTest` abaixo.