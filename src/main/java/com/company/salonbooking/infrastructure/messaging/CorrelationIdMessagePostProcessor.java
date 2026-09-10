package com.company.salonbooking.infrastructure.messaging;

import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.stereotype.Component;

import static com.company.salonbooking.infrastructure.web.CorrelationIdFilter.MDC_KEY;

/**
 * Attaches the current request's correlation ID (if any) as a message header on
 * every outbound RabbitMQ message (Seção 60: "Esse ID deve acompanhar... RabbitMQ
 * messages"). Applied centrally via RabbitTemplate so no publisher code needs to
 * remember to do this manually.
 */
@Component
public class CorrelationIdMessagePostProcessor implements MessagePostProcessor {

    public static final String CORRELATION_ID_HEADER = "x-correlation-id";

    @Override
    public Message postProcessMessage(Message message) {
        String correlationId = MDC.get(MDC_KEY);
        if (correlationId != null) {
            message.getMessageProperties().setHeader(CORRELATION_ID_HEADER, correlationId);
        }
        return message;
    }
}