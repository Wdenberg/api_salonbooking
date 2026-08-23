package com.company.salonbooking.employee.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Ad-hoc absence: vacation, day off, appointment outside the system, etc. (Seção 138). */
public final class AvailabilityBlock {

    private final UUID id;
    private final UUID employeeId;
    private final Instant startAt;
    private final Instant endAt;
    private final String reason;
    private final Instant createdAt;

    private AvailabilityBlock(UUID id, UUID employeeId, Instant startAt, Instant endAt, String reason, Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.employeeId = Objects.requireNonNull(employeeId);
        this.startAt = Objects.requireNonNull(startAt);
        this.endAt = Objects.requireNonNull(endAt);
        if (!startAt.isBefore(endAt)) {
            throw new IllegalArgumentException("startAt must be before endAt");
        }
        this.reason = reason;
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static AvailabilityBlock create(UUID id, UUID employeeId, Instant startAt, Instant endAt, String reason, Instant now) {
        return new AvailabilityBlock(id, employeeId, startAt, endAt, reason, now);
    }

    public static AvailabilityBlock restore(UUID id, UUID employeeId, Instant startAt, Instant endAt, String reason, Instant createdAt) {
        return new AvailabilityBlock(id, employeeId, startAt, endAt, reason, createdAt);
    }

    public boolean overlaps(Instant otherStart, Instant otherEnd) {
        return startAt.isBefore(otherEnd) && otherStart.isBefore(endAt);
    }

    public UUID getId() { return id; }
    public UUID getEmployeeId() { return employeeId; }
    public Instant getStartAt() { return startAt; }
    public Instant getEndAt() { return endAt; }
    public String getReason() { return reason; }
    public Instant getCreatedAt() { return createdAt; }
}