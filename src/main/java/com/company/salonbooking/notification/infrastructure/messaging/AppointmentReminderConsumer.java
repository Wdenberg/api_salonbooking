package com.company.salonbooking.notification.infrastructure.messaging;

import com.company.salonbooking.infrastructure.messaging.EventDeduplicationService;
import com.company.salonbooking.infrastructure.messaging.EventEnvelopeReader;
import com.company.salonbooking.infrastructure.messaging.RetryingMessageProcessor;
import com.company.salonbooking.notification.application.usecase.SendAppointmentNotificationUseCase;
import com.company.salonbooking.notification.infrastructure.reminder.ReminderType;
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
 * Consumes AppointmentReminderRequested events (published by ReminderSchedulerJob) and
 * sends the actual reminder. Deduplication here guards against RabbitMQ redelivery of
 * the SAME message (e.g. after a broker restart) — a different concern from
 * ReminderSchedulerJob's dedup log, which guards against generating the event twice
 * in the first place (Seção 34).
 */
@Component
public class AppointmentReminderConsumer {

    private static final Logger log = LoggerFactory.getLogger(AppointmentReminderConsumer.class);
    private static final String CONSUMER_NAME = "appointment-reminder-consumer";

    private final EventEnvelopeReader envelopeReader;
    private final EventDeduplicationService deduplicationService;
    private final RetryingMessageProcessor retryingMessageProcessor;
    private final SendAppointmentNotificationUseCase sendAppointmentNotificationUseCase;

    public AppointmentReminderConsumer(EventEnvelopeReader envelopeReader, EventDeduplicationService deduplicationService,
                                       RetryingMessageProcessor retryingMessageProcessor,
                                       SendAppointmentNotificationUseCase sendAppointmentNotificationUseCase) {
        this.envelopeReader = envelopeReader;
        this.deduplicationService = deduplicationService;
        this.retryingMessageProcessor = retryingMessageProcessor;
        this.sendAppointmentNotificationUseCase = sendAppointmentNotificationUseCase;
    }

    @RabbitListener(queues = APPOINTMENT_REMINDER_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void onMessage(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String body = new String(message.getBody(), StandardCharsets.UTF_8);

        try {
            EventEnvelopeReader.EnvelopeHeader envelope = envelopeReader.read(body);

            if (deduplicationService.alreadyProcessed(envelope.eventId(), CONSUMER_NAME)) {
                log.info("Skipping already-processed reminder event id={}", envelope.eventId());
                channel.basicAck(deliveryTag, false);
                return;
            }

            UUID appointmentId = UUID.fromString(envelope.payload().get("appointmentId").asText());
            String reminderType = envelope.payload().get("reminderType").asText();
            String label = ReminderType.valueOf(reminderType).label();

            sendAppointmentNotificationUseCase.sendReminder(appointmentId, label);

            if (!deduplicationService.markProcessed(envelope.eventId(), CONSUMER_NAME)) {
                log.info("Concurrent duplicate detected for reminder event id={}, discarding this delivery", envelope.eventId());
            }

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            channel.basicAck(deliveryTag, false);
            retryingMessageProcessor.handleFailure(message, APPOINTMENT_REMINDER_RETRY_EXCHANGE,
                    APPOINTMENT_REMINDER_RETRY_QUEUE, APPOINTMENT_REMINDER_DLQ, CONSUMER_NAME, e);
        }
    }
}