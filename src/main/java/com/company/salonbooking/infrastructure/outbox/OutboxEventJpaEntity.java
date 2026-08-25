package com.company.salonbooking.infrastructure.outbox;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEventJpaEntity {

    @Id
    private UUID id;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxStatus status;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt;

    protected OutboxEventJpaEntity() {
    }

    private OutboxEventJpaEntity(UUID id, String aggregateType, UUID aggregateId, String eventType, String payload,
                                 Instant createdAt) {
        this.id = id;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.attempts = 0;
        this.createdAt = createdAt;
        this.nextAttemptAt = createdAt; // eligible for dispatch immediately
    }

    public static OutboxEventJpaEntity create(UUID id, String aggregateType, UUID aggregateId, String eventType,
                                              String payload, Instant now) {
        return new OutboxEventJpaEntity(id, aggregateType, aggregateId, eventType, payload, now);
    }

    public void markPublished(Instant now) {
        this.status = OutboxStatus.PUBLISHED;
        this.processedAt = now;
    }

    /** Increments the attempt counter and either schedules a retry or gives up permanently (Seção 32). */
    public void registerFailure(Instant now, long backoffSeconds, int maxAttempts) {
        this.attempts += 1;
        if (this.attempts >= maxAttempts) {
            this.status = OutboxStatus.FAILED;
            this.processedAt = now;
        } else {
            this.status = OutboxStatus.PENDING;
            this.nextAttemptAt = now.plusSeconds(backoffSeconds);
        }
    }

    public UUID getId() { return id; }
    public String getAggregateType() { return aggregateType; }
    public UUID getAggregateId() { return aggregateId; }
    public String getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public OutboxStatus getStatus() { return status; }
    public int getAttempts() { return attempts; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getProcessedAt() { return processedAt; }
    public Instant getNextAttemptAt() { return nextAttemptAt; }
}