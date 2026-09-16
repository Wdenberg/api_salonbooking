# Salon Booking API - Implementation Plan

> Status tracking for the 22 audit items identified in the backend audit.

---

## ✅ IMPLEMENTED (Items 1-11, 13-14)

| Item | Description | Status | Key Changes |
|------|-------------|--------|-------------|
| **1** | **CORS Configuration** | ✅ Done | `CorsProperties`, `SecurityConfig.cors()`, configurable origins/methods/headers |
| **2** | **Rate Limiting** | ✅ Done | `RateLimitFilter` (Bucket4j), per-endpoint limits, headers (X-RateLimit-*) |
| **3** | **Refresh Token + Logout/Token Revocation** | ✅ Done | `refresh_tokens` & `access_token_blocklist` tables, rotation + reuse detection, `/auth/refresh`, `/auth/logout` |
| **4** | **DELETE Endpoints (Business, Employee, Service)** | ✅ Done | Soft delete (status change), ownership validation, active appointment checks |
| **5** | **Security Headers Explicitos** | ✅ Done | CSP, HSTS, X-Frame-Options, Referrer-Policy, Permissions-Policy in `SecurityConfig` |
| **6** | **Account Lockout / Brute-force Protection** | ✅ Done | `FailedLoginTracker` (Caffeine), 5 attempts / 15min lockout, HTTP 429 |
| **7** | **Unit Tests Isolados - CreateAppointmentUseCase** | ✅ Done | 6 new tests: employee schedule, availability block, pre-check conflict, cross-business validation |
| **8** | **Limpeza outbox_events (PUBLISHED antigos)** | ✅ Done | `OutboxCleanupJob` @Scheduled daily 3AM, configurable retention (30d default) |
| **9** | **Documentação OpenAPI Rica** | ✅ Done | `@Operation`, `@ApiResponses`, `@Schema` on all controllers + DTOs |
| **10** | **PLATFORM_ADMIN endpoints** | ✅ Done | `AdminAppointmentController`, `AdminBusinessController`, `AdminUserController` with cross-tenant queries |
| **11** | **GET `/api/v1/employees/{id}/availability-blocks`** | ✅ Done | `ListAvailabilityBlocksUseCase`, repository method, endpoint in `AvailabilityBlockController` |
| **13** | **GET `/api/v1/appointments` (global admin)** | ✅ Done | Implemented in `AdminAppointmentController` |
| **14** | **Endpoints Admin globais (cross-tenant)** | ✅ Done | Business, Users, Appointments listing for PLATFORM_ADMIN |

---

## 🔄 IN PROGRESS / PARTIAL

| Item | Description | Status | Notes |
|------|-------------|--------|-------|
| **12** | `@Size(min=8)` em password DTOs | ✅ Done | Já aplicado em `RegisterOwnerRequest`, `RegisterCustomerRequest`, `CreateEmployeeRequest` |

---

## ⏳ PENDING (Items 15-22)

| Item | Description | Priority | Dependencies |
|------|-------------|----------|--------------|
| **15** | **Refresh Token Rotation Testes** | 🟢 Low | Testes de rotação e detecção de reuso |
| **16** | **Outbox Cleanup Testes** | 🟢 Low | Teste do job de limpeza |
| **17** | **Rate Limit Testes** | 🟢 Low | Testes de throttling |
| **18** | **Account Lockout Testes** | 🟢 Low | Testes de bloqueio/desbloqueio |
| **19** | **DELETE Business/Employee/Service Testes** | 🟢 Low | Testes de soft delete |
| **20** | **CORS Testes** | 🟢 Low | Testes de preflight/origins |
| **21** | **Security Headers Testes** | 🟢 Low | Verificação headers na resposta |
| **22** | **Arquitetura/Arquivos de documentação finais** | 🟢 Low | README, ADRs já existem |

---

## 📋 CHECKLIST DE TESTES PENDENTES

```text
[ ] Testes PLATFORM_ADMIN endpoints
[x] Testes GET /employees/{id}/availability-blocks
[ ] Testes GET /appointments (admin global)
[ ] Testes Refresh Token Rotation (reuse detection)
[ ] Testes Outbox Cleanup Job
[ ] Testes Rate Limiting (429 responses)
[ ] Testes Account Lockout (429 after 5 failures)
[ ] Testes DELETE endpoints (soft delete, validation)
[ ] Testes CORS (preflight, allowed origins)
[ ] Testes Security Headers (CSP, HSTS, etc.)
[ ] Testes Integration completos (requerem Docker)
```

---

## 📊 MÉTRICAS ATUAIS

| Métrica | Valor |
|---------|-------|
| **Testes Unitários Passando** | 45 |
| **Testes Integração (sem Docker)** | Falham (esperado) |
| **Cobertura JaCoCo (scheduling)** | ≥ 75% (enforced) |
| **Quality Gates** | Checkstyle ✅, SpotBugs ✅, JaCoCo ✅, OWASP ✅ |
| **Build** | ✅ Success |
| **Docker** | ✅ Multi-stage, non-root, healthcheck |

---

## 🎯 PRÓXIMOS PASSOS RECOMENDADOS

1. **Adicionar testes unitários** para os novos recursos (Items 11, 15-21)
2. **Validar em ambiente com Docker** (Testcontainers, integração completa)
3. **Deploy staging** e testes de carga

---

## 📝 NOTAS TÉCNICAS

- **Java 25**, **Spring Boot 4.1.0**, **PostgreSQL 18**, **RabbitMQ 4**
- **Arquitetura**: Modular Monolith + Hexagonal + DDD + Event-Driven
- **Segurança**: JWT stateless + BCrypt + Ownership Authorization + Rate Limit + Account Lockout
- **Observabilidade**: Actuator + Prometheus + Structured Logging (ECS) + Correlation ID
- **Transacional**: Transactional Outbox + Exclusion Constraint (double booking prevention)

---

*Última atualização: 2026-09-16*