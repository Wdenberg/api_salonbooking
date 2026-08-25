package com.company.salonbooking.shared.domain.event;

import java.util.UUID;

/**
 * Contrato que todo evento de domínio deve implementar para ser roteado por meio da Outbox
 * (Seção 93: Eventos de domínio são internos; este é o objeto de fronteira que é
 * encapsulado em um envelope e serializado — o domínio em si nunca interage
 * diretamente com o RabbitMQ ou com JSON).
 */
public interface DomainEvent {

    String eventType();

    String aggregateType();

    UUID aggregateId();

    default int version() {
        return 1;
    }
}