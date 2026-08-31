package com.company.salonbooking.notification.application.usecase;

import com.company.salonbooking.notification.application.port.AppointmentContextResolver;
import com.company.salonbooking.notification.application.port.NotificationProvider;
import com.company.salonbooking.notification.domain.model.Notification;
import com.company.salonbooking.notification.domain.model.NotificationType;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

/**
 * Composes and sends the notification for a given appointment lifecycle event. Kept
 * transport-agnostic: it never touches RabbitMQ directly — it is called by the
 * consumer (infrastructure layer) after a message has already been received and
 * deduplicated (Seção 33: "O domínio não deve conhecer detalhes do provedor").
 */
@Service
public class SendAppointmentNotificationUseCase {

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneOffset.UTC);

    private final AppointmentContextResolver contextResolver;
    private final NotificationProvider notificationProvider;

    public SendAppointmentNotificationUseCase(AppointmentContextResolver contextResolver, NotificationProvider notificationProvider) {
        this.contextResolver = contextResolver;
        this.notificationProvider = notificationProvider;
    }

    public void sendCreated(UUID appointmentId) {
        var ctx = contextResolver.resolve(appointmentId);
        notificationProvider.send(new Notification(ctx.customerId(), NotificationType.APPOINTMENT_CREATED,
                "Agendamento recebido",
                "Seu agendamento de " + ctx.serviceName() + " com " + ctx.employeeName() + " em " + ctx.businessName() +
                        " foi recebido para " + DISPLAY_FORMAT.format(ctx.startAt()) + ". Aguardando confirmação.",
                Map.of("appointmentId", ctx.appointmentId().toString())));
    }

    public void sendConfirmed(UUID appointmentId) {
        var ctx = contextResolver.resolve(appointmentId);
        notificationProvider.send(new Notification(ctx.customerId(), NotificationType.APPOINTMENT_CONFIRMED,
                "Agendamento confirmado",
                "Seu agendamento de " + ctx.serviceName() + " com " + ctx.employeeName() + " em " + ctx.businessName() +
                        " foi confirmado para " + DISPLAY_FORMAT.format(ctx.startAt()) + ".",
                Map.of("appointmentId", ctx.appointmentId().toString())));
    }

    public void sendCancelled(UUID appointmentId) {
        var ctx = contextResolver.resolve(appointmentId);
        notificationProvider.send(new Notification(ctx.customerId(), NotificationType.APPOINTMENT_CANCELLED,
                "Agendamento cancelado",
                "Seu agendamento de " + ctx.serviceName() + " em " + ctx.businessName() +
                        " previsto para " + DISPLAY_FORMAT.format(ctx.startAt()) + " foi cancelado.",
                Map.of("appointmentId", ctx.appointmentId().toString())));
    }

    public void sendReminder(UUID appointmentId, String reminderLabel) {
        var ctx = contextResolver.resolve(appointmentId);
        notificationProvider.send(new Notification(ctx.customerId(), NotificationType.APPOINTMENT_REMINDER,
                "Lembrete de agendamento",
                "Lembrete: seu agendamento de " + ctx.serviceName() + " com " + ctx.employeeName() + " em " +
                        ctx.businessName() + " é " + reminderLabel + ", às " + DISPLAY_FORMAT.format(ctx.startAt()) + ".",
                Map.of("appointmentId", ctx.appointmentId().toString(), "reminderType", reminderLabel)));
    }
}