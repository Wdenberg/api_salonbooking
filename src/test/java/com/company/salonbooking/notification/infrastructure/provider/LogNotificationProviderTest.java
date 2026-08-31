package com.company.salonbooking.notification.infrastructure.provider;

import com.company.salonbooking.notification.domain.model.Notification;
import com.company.salonbooking.notification.domain.model.NotificationType;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

/** Smoke test: ensures the fake provider never throws for a well-formed notification. */
class LogNotificationProviderTest {

    @Test
    void naoDeveLancarExcecaoAoEnviar() {
        LogNotificationProvider provider = new LogNotificationProvider();

        Notification notification = new Notification(UUID.randomUUID(), NotificationType.APPOINTMENT_CONFIRMED,
                "Assunto", "Corpo da mensagem", Map.of("appointmentId", UUID.randomUUID().toString()));

        provider.send(notification);
    }
}