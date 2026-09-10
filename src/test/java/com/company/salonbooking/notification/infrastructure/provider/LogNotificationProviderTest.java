package com.company.salonbooking.notification.infrastructure.provider;

import com.company.salonbooking.infrastructure.metrics.AppMetrics;
import com.company.salonbooking.notification.domain.model.Notification;
import com.company.salonbooking.notification.domain.model.NotificationType;
import org.hibernate.boot.internal.Extends;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.testcontainers.shaded.org.bouncycastle.oer.its.ieee1609dot2.EndEntityType.app;

/** Smoke test: ensures the fake provider never throws for a well-formed notification. */
@ExtendWith(MockitoExtension.class)
class LogNotificationProviderTest {

    @Mock
    private AppMetrics appMetrics;
    @Test
    void naoDeveLancarExcecaoAoEnviar() {
        LogNotificationProvider provider = new LogNotificationProvider(appMetrics);

        Notification notification = new Notification(
                UUID.randomUUID(),
                NotificationType.APPOINTMENT_CONFIRMED,
                "Assunto", "Corpo da mensagem", Map.of("appointmentId", UUID.randomUUID().toString()));

        provider.send(notification);
    }
}