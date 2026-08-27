package com.company.salonbooking.infrastructure.messaging;

import java.util.UUID;

/**
 * Porta da qual o OutboxPublisherJob depende para efetivamente entregar um evento.
 * Atualmente implementada pelo LogMessageBroker (Fase 8); a Fase 9 substitui esse bean
 * por um adaptador baseado em RabbitMQ que roteia para as exchanges definidas na
 * Seção 31 — a lógica do job de publicação não sofre qualquer alteração com essa substituição.
 */

public interface MessageBroker {

    void publish(String eventType, UUID eventId,  UUID aggregateId, String payloadJson);
}