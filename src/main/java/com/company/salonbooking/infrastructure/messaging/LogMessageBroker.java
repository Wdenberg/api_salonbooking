package com.company.salonbooking.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Implementação de desenvolvimento/placeholder (mesmo padrão do LogNotificationProvider da Seção 33):
 * registra logs em vez de realizar a entrega efetiva. Isso permite testar integralmente
 * o loop de despacho da caixa de saída, as tentativas de reenvio e os mecanismos de
 * bloqueio antes da integração com o RabbitMQ na Fase 9.
 */
@Component
public class LogMessageBroker implements MessageBroker {

    private static final Logger log = LoggerFactory.getLogger(LogMessageBroker.class);

    @Override
    public void publish(String eventType, UUID aggregateId, String payloadJson) {
        log.info("[outbox-stub] would publish event type={} aggregateId={}", eventType, aggregateId);
    }
}