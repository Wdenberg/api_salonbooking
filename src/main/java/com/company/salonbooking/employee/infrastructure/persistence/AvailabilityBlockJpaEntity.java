package com.company.salonbooking.employee.infrastructure.persistence;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "availability_blocks")
public class AvailabilityBlockJpaEntity {

    @Id
    private UUID id;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @Column(length = 200)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AvailabilityBlockJpaEntity() {
    }

    public AvailabilityBlockJpaEntity(UUID id, UUID employeeId, Instant startAt, Instant endAt, String reason, Instant createdAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getEmployeeId() { return employeeId; }
    public Instant getStartAt() { return startAt; }
    public Instant getEndAt() { return endAt; }
    public String getReason() { return reason; }
    public Instant getCreatedAt() { return createdAt; }
}