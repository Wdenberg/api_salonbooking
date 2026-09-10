package com.company.salonbooking.notification.infrastructure.provider;

import com.company.salonbooking.infrastructure.metrics.AppMetrics;
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
    private final AppMetrics appMetrics;

    public LogNotificationProvider(AppMetrics appMetrics) {
        this.appMetrics = appMetrics;
    }

    @Override
    public void send(Notification notification) {
        try {
            log.info("[notification] to userId={} type={} subject=\"{}\" body=\"{}\"",
                    notification.getRecipientUserId(), notification.getType(), notification.getSubject(), notification.getBody());
            appMetrics.incrementNotificationFailed();
        }catch (Exception e){
            appMetrics.incrementNotificationFailed();
            throw e;
        }
    }
}