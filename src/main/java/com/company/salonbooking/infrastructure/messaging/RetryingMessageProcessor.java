package com.company.salonbooking.infrastructure.messaging;

import com.company.salonbooking.infrastructure.outbox.OutboxBackoffCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Shared retry/DLQ escalation logic used by every consumer (Seção 32, 109). A consumer
 * calls handleFailure() when message processing throws; this class decides whether to
 * republish to the retry exchange with a backoff-scaled TTL, or give up and route to
 * the final DLQ once maxAttempts is reached.
 *
 * The retry-count is tracked via the "x-retry-count" custom header rather than
 * RabbitMQ's built-in x-death array, because x-death accumulates across every queue a
 * message ever passed through and is awkward to reason about across multiple retry
 * hops — a single explicit counter is simpler to test and reason about.
 */
@Component
public class RetryingMessageProcessor {

    private static final Logger log = LoggerFactory.getLogger(RetryingMessageProcessor.class);
    private static final String RETRY_COUNT_HEADER = "x-retry-count";

    private static final int MAX_ATTEMPTS = 5;
    private static final long INITIAL_BACKOFF_SECONDS = 5;
    private static final long MAX_BACKOFF_SECONDS = 300;

    private final RabbitTemplate rabbitTemplate;

    public RetryingMessageProcessor(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void handleFailure(Message message, String retryExchange, String retryRoutingKey, String finalDlq,
                              String consumerName, Exception cause) {
        int attempt = currentAttempt(message) + 1;

        if (attempt >= MAX_ATTEMPTS) {
            log.error("Consumer {} exhausted {} attempts, routing to DLQ {}: {}",
                    consumerName, MAX_ATTEMPTS, finalDlq, cause.getMessage());
            publishToFinalDlq(message, finalDlq, attempt);
            return;
        }

        long backoffSeconds = OutboxBackoffCalculator.computeBackoffSeconds(attempt, INITIAL_BACKOFF_SECONDS, MAX_BACKOFF_SECONDS);
        log.warn("Consumer {} attempt {}/{} failed, retrying in {}s: {}",
                consumerName, attempt, MAX_ATTEMPTS, backoffSeconds, cause.getMessage());

        Message retryMessage = buildRetryMessage(message, attempt, backoffSeconds);
        rabbitTemplate.send(retryExchange, retryRoutingKey, retryMessage);
    }

    private int currentAttempt(Message message) {
        Object header = message.getMessageProperties().getHeaders().get(RETRY_COUNT_HEADER);
        return header instanceof Integer i ? i : 0;
    }

    private Message buildRetryMessage(Message original, int attempt, long backoffSeconds) {
        MessageProperties props = MessagePropertiesBuilderCopy.copy(original.getMessageProperties());
        props.setHeader(RETRY_COUNT_HEADER, attempt);
        props.setExpiration(String.valueOf(backoffSeconds * 1000)); // per-message TTL, in milliseconds
        return new Message(original.getBody(), props);
    }

    private void publishToFinalDlq(Message original, String finalDlq, int attempt) {
        MessageProperties props = MessagePropertiesBuilderCopy.copy(original.getMessageProperties());
        props.setHeader(RETRY_COUNT_HEADER, attempt);
        Message dlqMessage = new Message(original.getBody(), props);
        rabbitTemplate.send("", finalDlq, dlqMessage); // default exchange routes by queue name
    }
}