package com.company.salonbooking.notification.application.port;

import com.company.salonbooking.notification.domain.model.Notification;

/**
 * The abstraction described in Seção 33. Concrete channels (Email, WhatsApp, SMS, Push)
 * are future adapters implementing this same interface — the domain and application
 * layers never change when a new channel is added, only a new adapter + wiring.
 */
public interface NotificationProvider {

    void send(Notification notification);
}