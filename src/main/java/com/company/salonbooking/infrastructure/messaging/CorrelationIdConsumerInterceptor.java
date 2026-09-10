package com.company.salonbooking.infrastructure.messaging;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.company.salonbooking.infrastructure.messaging.CorrelationIdMessagePostProcessor.CORRELATION_ID_HEADER;
import static com.company.salonbooking.infrastructure.web.CorrelationIdFilter.MDC_KEY;

/**
 * Restores the correlation ID into MDC on the consumer thread before a @RabbitListener
 * method runs, so every log line emitted while processing a message — including inside
 * use cases invoked by the consumer — carries the same correlationId as the original
 * HTTP request that triggered the event (Seção 60's "ponta a ponta" tracing).
 *
 * Wired as a MethodInterceptor around SimpleRabbitListenerContainerFactory's listener
 * invocation rather than duplicating this logic inside every consumer class.
 */
@Component
public class CorrelationIdConsumerInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        String correlationId = extractCorrelationId(invocation.getArguments());
        boolean setHere = false;

        try {
            if (correlationId != null) {
                MDC.put(MDC_KEY, correlationId);
                setHere = true;
            }
            return invocation.proceed();
        } finally {
            if (setHere) {
                MDC.remove(MDC_KEY);
            }
        }
    }

    private String extractCorrelationId(Object[] arguments) {
        for (Object arg : arguments) {
            if (arg instanceof Message message) {
                Object header = message.getMessageProperties().getHeaders().get(CORRELATION_ID_HEADER);
                if (header != null) {
                    return header.toString();
                }
                // Fallback: if no correlation ID travelled with the message (e.g. a
                // message published before this feature existed), generate one so the
                // consumer's own processing is still traceable end to end.
                return UUID.randomUUID().toString();
            }
        }
        return null;
    }
}