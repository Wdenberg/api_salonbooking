package com.company.salonbooking.shared.domain.event;

import java.time.Instant;
import java.util.UUID;

/** Formato de transmissão padronizado para cada evento (Seção 29). */
public record DomainEventEnvelope(
        UUID eventId,
        String eventType,
        int version,
        Instant occurredAt,
        String aggregateType,
        UUID aggregateId,
        Object payload
) {}