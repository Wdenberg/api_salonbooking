package com.company.salonbooking.notification.infrastructure.messaging;

import com.company.salonbooking.infrastructure.messaging.EventDeduplicationService;
import com.company.salonbooking.infrastructure.messaging.EventEnvelopeReader;
import com.company.salonbooking.infrastructure.messaging.RetryingMessageProcessor;
import com.company.salonbooking.notification.application.usecase.SendAppointmentNotificationUseCase;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static com.company.salonbooking.infrastructure.messaging.RabbitMqTopology.*;

/**
 * Listens on the appointment.events exchange (routed via appointment.notification.queue,
 * bound to routing key "appointment.*" since Fase 9) and dispatches the corresponding
 * customer-facing notification. The ack/retry/dedup skeleton is unchanged from Fase 9 —
 * only processEvent() now does real work instead of logging a stub line.
 */
@Component
public class AppointmentNotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(AppointmentNotificationConsumer.class);
    private static final String CONSUMER_NAME = "appointment-notification-consumer";

    private final EventEnvelopeReader envelopeReader;
    private final EventDeduplicationService deduplicationService;
    private final RetryingMessageProcessor retryingMessageProcessor;
    private final SendAppointmentNotificationUseCase sendAppointmentNotificationUseCase;

    public AppointmentNotificationConsumer(EventEnvelopeReader envelopeReader, EventDeduplicationService deduplicationService,
                                           RetryingMessageProcessor retryingMessageProcessor,
                                           SendAppointmentNotificationUseCase sendAppointmentNotificationUseCase) {
        this.envelopeReader = envelopeReader;
        this.deduplicationService = deduplicationService;
        this.retryingMessageProcessor = retryingMessageProcessor;
        this.sendAppointmentNotificationUseCase = sendAppointmentNotificationUseCase;
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
                log.info("Concurrent duplicate detected for event id={}, discarding this delivery", envelope.eventId());
            }

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            channel.basicAck(deliveryTag, false);
            retryingMessageProcessor.handleFailure(message, APPOINTMENT_NOTIFICATION_RETRY_EXCHANGE,
                    APPOINTMENT_NOTIFICATION_RETRY_QUEUE, APPOINTMENT_NOTIFICATION_DLQ, CONSUMER_NAME, e);
        }
    }

    private void processEvent(EventEnvelopeReader.EnvelopeHeader envelope) {
        UUID appointmentId = envelope.aggregateId();

        switch (envelope.eventType()) {
            case "AppointmentCreated" ->
                    sendAppointmentNotificationUseCase.sendCreated(appointmentId);

            case "AppointmentConfirmed" ->
                    sendAppointmentNotificationUseCase.sendConfirmed(appointmentId);

            case "AppointmentCancelled" ->
                    sendAppointmentNotificationUseCase.sendCancelled(appointmentId);

            default ->
                    log.debug(
                            "No notification handler for event type={}",
                            envelope.eventType()
                    );
        }
    }
}