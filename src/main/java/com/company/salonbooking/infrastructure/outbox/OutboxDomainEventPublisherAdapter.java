package com.company.salonbooking.infrastructure.outbox;

import com.company.salonbooking.shared.application.port.DomainEventPublisher;
import com.company.salonbooking.shared.domain.event.DomainEvent;
import com.company.salonbooking.shared.domain.event.DomainEventEnvelope;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/**
 * Grava diretamente na tabela de outbox usando um simples repository.save() — deliberadamente
 * SEM a anotação @Transactional aqui. Este método deve sempre ser executado dentro da transação
 * existente do caso de uso chamador (Seção 25/26), compartilhando a mesma conexão, para que
 * o registro do agendamento e o registro correspondente na outbox sejam confirmados (commit)
 * ou revertidos (rollback) juntos, de forma atômica.
 */
@Component
public class OutboxDomainEventPublisherAdapter implements DomainEventPublisher {

    private final OutboxEventJpaRepository repository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public OutboxDomainEventPublisherAdapter(OutboxEventJpaRepository repository, ObjectMapper objectMapper, Clock clock) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    public void publish(DomainEvent event) {
        Instant now = Instant.now(clock);

        DomainEventEnvelope envelope = new DomainEventEnvelope(
                UUID.randomUUID(), event.eventType(), event.version(), now, event.aggregateType(),
                event.aggregateId(), event);

        String json = serialize(envelope);

        OutboxEventJpaEntity entity = OutboxEventJpaEntity.create(
                UUID.randomUUID(), event.aggregateType(), event.aggregateId(), event.eventType(), json, now);

        repository.save(entity);
    }

    private String serialize(DomainEventEnvelope envelope) {
        try {
            return objectMapper.writeValueAsString(envelope);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize domain event envelope for " + envelope.eventType(), e);
        }
    }
}