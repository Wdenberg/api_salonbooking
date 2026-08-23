package com.company.salonbooking.employee.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Employee {

    private final UUID id;
    private final UUID userId;
    private final UUID businessId;
    private String specialty;
    private EmployeeStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private Employee(UUID id, UUID userId, UUID businessId, String specialty, EmployeeStatus status,
                     Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.userId = Objects.requireNonNull(userId);
        this.businessId = Objects.requireNonNull(businessId);
        this.specialty = specialty;
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Employee create(UUID id, UUID userId, UUID businessId, String specialty, Instant now) {
        return new Employee(id, userId, businessId, specialty, EmployeeStatus.ACTIVE, now, now);
    }

    public static Employee restore(UUID id, UUID userId, UUID businessId, String specialty, EmployeeStatus status,
                                   Instant createdAt, Instant updatedAt) {
        return new Employee(id, userId, businessId, specialty, status, createdAt, updatedAt);
    }

    public void updateSpecialty(String specialty, Instant now) {
        this.specialty = specialty;
        this.updatedAt = now;
    }

    public void changeStatus(EmployeeStatus newStatus, Instant now) {
        this.status = Objects.requireNonNull(newStatus);
        this.updatedAt = now;
    }

    public boolean isActive() {
        return status == EmployeeStatus.ACTIVE;
    }

    public boolean belongsToBusiness(UUID businessIdToCheck) {
        return businessId.equals(businessIdToCheck);
    }

    public boolean isUser(UUID userIdToCheck) {
        return userId.equals(userIdToCheck);
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getBusinessId() { return businessId; }
    public String getSpecialty() { return specialty; }
    public EmployeeStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee employee)) return false;
        return id.equals(employee.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}