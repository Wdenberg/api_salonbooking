package com.company.salonbooking.notification.infrastructure.provider;

import com.company.salonbooking.notification.application.port.NotificationProvider;
import com.company.salonbooking.notification.domain.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Development/fake implementation (Seção 33: "Inicialmente criar LogNotificationProvider").
 * Real channels (EmailNotificationProvider, WhatsAppNotificationProvider,
 * SmsNotificationProvider, PushNotificationProvider) are future adapters behind the
 * same NotificationProvider port — swapping this bean out is the only change needed.
 */
@Component
public class LogNotificationProvider implements NotificationProvider {

    private static final Logger log = LoggerFactory.getLogger(LogNotificationProvider.class);

    @Override
    public void send(Notification notification) {
        log.info("[notification] to userId={} type={} subject=\"{}\" body=\"{}\"",
                notification.getRecipientUserId(), notification.getType(), notification.getSubject(), notification.getBody());
    }
}