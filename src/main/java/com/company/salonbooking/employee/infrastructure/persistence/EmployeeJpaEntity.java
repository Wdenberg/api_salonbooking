package com.company.salonbooking.employee.infrastructure.persistence;

import com.company.salonbooking.employee.domain.model.EmployeeStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "employees")
public class EmployeeJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Column(length = 150)
    private String specialty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmployeeStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected EmployeeJpaEntity() {
    }

    public EmployeeJpaEntity(UUID id, UUID userId, UUID businessId, String specialty, EmployeeStatus status,
                             Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.businessId = businessId;
        this.specialty = specialty;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getBusinessId() { return businessId; }
    public String getSpecialty() { return specialty; }
    public EmployeeStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}