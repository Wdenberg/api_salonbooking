package com.company.salonbooking.infrastructure.messaging;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ProcessedEventId implements Serializable {

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(nullable = false, length = 150)
    private String consumer;

    protected ProcessedEventId() {
    }

    public ProcessedEventId(UUID eventId, String consumer) {
        this.eventId = eventId;
        this.consumer = consumer;
    }

    public UUID getEventId() { return eventId; }
    public String getConsumer() { return consumer; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProcessedEventId that)) return false;
        return eventId.equals(that.eventId) && consumer.equals(that.consumer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, consumer);
    }
}