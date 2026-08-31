package com.company.salonbooking.notification.application.usecase;

import com.company.salonbooking.notification.application.port.AppointmentContextResolver;
import com.company.salonbooking.notification.application.port.NotificationProvider;
import com.company.salonbooking.notification.domain.model.Notification;
import com.company.salonbooking.notification.domain.model.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendAppointmentNotificationUseCaseTest {

    @Mock private AppointmentContextResolver contextResolver;
    @Mock private NotificationProvider notificationProvider;

    private SendAppointmentNotificationUseCase useCase;
    private UUID appointmentId;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        useCase = new SendAppointmentNotificationUseCase(contextResolver, notificationProvider);
        appointmentId = UUID.randomUUID();
        customerId = UUID.randomUUID();

        var context = new AppointmentContextResolver.AppointmentContext(
                appointmentId, customerId, UUID.randomUUID(), UUID.randomUUID(),
                "Barbearia Central", "Corte", "Zé", Instant.parse("2026-08-20T14:00:00Z"));
        when(contextResolver.resolve(appointmentId)).thenReturn(context);
    }

    @Test
    void deveEnviarNotificacaoDeConfirmacaoParaOClienteCorreto() {
        useCase.sendConfirmed(appointmentId);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationProvider).send(captor.capture());

        Notification sent = captor.getValue();
        assertThat(sent.getRecipientUserId()).isEqualTo(customerId);
        assertThat(sent.getType()).isEqualTo(NotificationType.APPOINTMENT_CONFIRMED);
        assertThat(sent.getBody()).contains("Corte").contains("Zé").contains("Barbearia Central");
    }

    @Test
    void deveEnviarLembreteComORotuloCorreto() {
        useCase.sendReminder(appointmentId, "24 horas antes");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationProvider).send(captor.capture());

        assertThat(captor.getValue().getType()).isEqualTo(NotificationType.APPOINTMENT_REMINDER);
        assertThat(captor.getValue().getBody()).contains("24 horas antes");
    }
}