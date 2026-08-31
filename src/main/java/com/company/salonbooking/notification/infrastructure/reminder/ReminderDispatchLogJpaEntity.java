package com.company.salonbooking.notification.infrastructure.reminder;

import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

import java.time.Instant;

@Entity
@Table(name = "reminder_dispatch_log")
public class ReminderDispatchLogJpaEntity
        implements Persistable<ReminderDispatchLogId> {

    @EmbeddedId
    private ReminderDispatchLogId id;

    @Column(name = "dispatched_at", nullable = false)
    private Instant dispatchedAt;

    @Transient
    private boolean isNew = true;

    protected ReminderDispatchLogJpaEntity() {
    }

    public ReminderDispatchLogJpaEntity(
            ReminderDispatchLogId id,
            Instant dispatchedAt) {
        this.id = id;
        this.dispatchedAt = dispatchedAt;
    }

    @Override
    public ReminderDispatchLogId getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }

    public Instant getDispatchedAt() {
        return dispatchedAt;
    }
}