package com.company.salonbooking.notification.domain.model;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable message to be delivered. The domain never knows about the actual
 * transport (Twilio, SES, SendGrid, WhatsApp API — Seção 114); it only produces
 * this value object, which a NotificationProvider implementation interprets.
 */
public final class Notification {

    private final UUID recipientUserId;
    private final NotificationType type;
    private final String subject;
    private final String body;
    private final Map<String, String> metadata;

    public Notification(UUID recipientUserId, NotificationType type, String subject, String body,
                        Map<String, String> metadata) {
        this.recipientUserId = Objects.requireNonNull(recipientUserId);
        this.type = Objects.requireNonNull(type);
        this.subject = Objects.requireNonNull(subject);
        this.body = Objects.requireNonNull(body);
        this.metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public UUID getRecipientUserId() { return recipientUserId; }
    public NotificationType getType() { return type; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public Map<String, String> getMetadata() { return metadata; }
}