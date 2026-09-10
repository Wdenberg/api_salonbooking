package com.company.salonbooking.infrastructure.messaging;

import com.company.salonbooking.notification.application.usecase.SendAppointmentNotificationUseCase;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


import java.nio.charset.StandardCharsets;
import java.time.Instant;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import static com.company.salonbooking.infrastructure.messaging.RabbitMqTopology.*;

/**
 * Proves, against a real RabbitMQ broker (Testcontainers — Seção 71), that a message
 * failing every retry attempt actually lands in the final DLQ — not just that
 * RetryingMessageProcessor's code path was exercised in isolation (Fase 9's unit tests
 * covered the calculation; this covers the end-to-end routing).
 */
@Tag("slow")
class DeadLetterQueueIntegrationTest extends AbstractRabbitMqIntegrationTest {

    @Autowired private RabbitTemplate rabbitTemplate;

    @MockitoBean
    private SendAppointmentNotificationUseCase sendAppointmentNotificationUseCase;

    @Test
    void mensagemQueSempreFalhaDeveChegarNaDlqAposEsgotarTentativas() {
        doThrow(new RuntimeException("simulated permanent failure"))
                .when(sendAppointmentNotificationUseCase).sendCreated(any(UUID.class));

        UUID eventId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();
        String payload = """
                {"eventId":"%s","eventType":"AppointmentCreated","version":1,"occurredAt":"%s",
                 "aggregateType":"Appointment","aggregateId":"%s",
                 "payload":{"appointmentId":"%s"}}
                """.formatted(eventId, Instant.now(), appointmentId, appointmentId);

        MessageProperties props = new MessageProperties();
        props.setContentType("application/json");
        Message message = new Message(payload.getBytes(StandardCharsets.UTF_8), props);

        rabbitTemplate.send(APPOINTMENT_EVENTS_EXCHANGE, "appointment.created", message);

        // 5 attempts with backoffs of 5s/10s/20s/40s (per OutboxBackoffCalculator) means
        // this genuinely takes time; the test waits generously rather than trying to
        // fast-forward a real broker's TTL-based redelivery.
        await().atMost(90, TimeUnit.SECONDS).untilAsserted(() -> {
            Message dlqMessage = rabbitTemplate.receive(APPOINTMENT_NOTIFICATION_DLQ, 1000);
            assertThat(dlqMessage).isNotNull();
        });
    }
}