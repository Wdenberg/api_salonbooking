package com.company.salonbooking.infrastructure.messaging;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "processed_events")
public class ProcessedEventJpaEntity {

    @EmbeddedId
    private ProcessedEventId id;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    protected ProcessedEventJpaEntity() {
    }

    public ProcessedEventJpaEntity(ProcessedEventId id, Instant processedAt) {
        this.id = id;
        this.processedAt = processedAt;
    }

    public ProcessedEventId getId() { return id; }
    public Instant getProcessedAt() { return processedAt; }
}