package com.company.salonbooking.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * No longer a Spring bean as of Fase 9 — RabbitMqMessageBroker is now the active
 * MessageBroker implementation. Kept in the codebase as a documented reference for
 * local development without RabbitMQ (wire it up manually with @Component if needed
 * and remove @Component from RabbitMqMessageBroker temporarily).
 */
public class LogMessageBroker implements MessageBroker {

    private static final Logger log = LoggerFactory.getLogger(LogMessageBroker.class);

    @Override
    public void publish(String eventType, UUID eventId, UUID aggregateId, String payloadJson) {
        log.info("[log-broker] would publish event type={} eventId={} aggregateId={}",
                eventType, eventId, aggregateId);
    }
}