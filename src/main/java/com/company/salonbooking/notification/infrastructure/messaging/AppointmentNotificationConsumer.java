package com.company.salonbooking.notification.infrastructure.messaging;

import com.company.salonbooking.infrastructure.messaging.EventDeduplicationService;
import com.company.salonbooking.infrastructure.messaging.EventEnvelopeReader;
import com.company.salonbooking.infrastructure.messaging.RetryingMessageProcessor;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.company.salonbooking.infrastructure.messaging.RabbitMqTopology.*;

/**
 * Placeholder consumer (Fase 9) that proves the full retry/DLQ/dedup pipeline end to
 * end. Fase 10 replaces the body of processEvent() with a call to NotificationProvider
 * instead of a log line — the surrounding ack/retry/dedup machinery does not change.
 */
@Component
public class AppointmentNotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(AppointmentNotificationConsumer.class);
    private static final String CONSUMER_NAME = "appointment-notification-consumer";

    private final EventEnvelopeReader envelopeReader;
    private final EventDeduplicationService deduplicationService;
    private final RetryingMessageProcessor retryingMessageProcessor;

    public AppointmentNotificationConsumer(EventEnvelopeReader envelopeReader, EventDeduplicationService deduplicationService,
                                           RetryingMessageProcessor retryingMessageProcessor) {
        this.envelopeReader = envelopeReader;
        this.deduplicationService = deduplicationService;
        this.retryingMessageProcessor = retryingMessageProcessor;
    }

    @RabbitListener(queues = APPOINTMENT_NOTIFICATION_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void onMessage(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String body = new String(message.getBody(), StandardCharsets.UTF_8);

        try {
            EventEnvelopeReader.EnvelopeHeader envelope = envelopeReader.read(body);

            if (deduplicationService.alreadyProcessed(envelope.eventId(), CONSUMER_NAME)) {
                log.info("Skipping already-processed event id={} type={}", envelope.eventId(), envelope.eventType());
                channel.basicAck(deliveryTag, false);
                return;
            }

            processEvent(envelope);

            if (!deduplicationService.markProcessed(envelope.eventId(), CONSUMER_NAME)) {
                // Lost a race with another delivery of the same message; effect already
                // recorded by the other attempt, so this one is still safe to ack.
                log.info("Concurrent duplicate detected for event id={}, discarding this delivery", envelope.eventId());
            }

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            channel.basicAck(deliveryTag, false); // remove from main queue; retry handled explicitly below
            retryingMessageProcessor.handleFailure(message, APPOINTMENT_NOTIFICATION_RETRY_EXCHANGE,
                    APPOINTMENT_NOTIFICATION_RETRY_QUEUE, APPOINTMENT_NOTIFICATION_DLQ, CONSUMER_NAME, e);
        }
    }

    /** Fase 10 replaces this with real notification dispatch via NotificationProvider. */
    private void processEvent(EventEnvelopeReader.EnvelopeHeader envelope) {
        log.info("[notification-stub] would notify for event type={} payload={}", envelope.eventType(), envelope.payload());
    }
}