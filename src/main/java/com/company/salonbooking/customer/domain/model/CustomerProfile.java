package com.company.salonbooking.customer.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/** Minimal data footprint, per data minimization principle (Seção 91). */
public final class CustomerProfile {

    private final UUID userId;
    private String phone;
    private LocalDate dateOfBirth;
    private final Instant createdAt;
    private Instant updatedAt;

    private CustomerProfile(UUID userId, String phone, LocalDate dateOfBirth, Instant createdAt, Instant updatedAt) {
        this.userId = Objects.requireNonNull(userId);
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static CustomerProfile createEmpty(UUID userId, Instant now) {
        return new CustomerProfile(userId, null, null, now, now);
    }

    public static CustomerProfile restore(UUID userId, String phone, LocalDate dateOfBirth, Instant createdAt, Instant updatedAt) {
        return new CustomerProfile(userId, phone, dateOfBirth, createdAt, updatedAt);
    }

    public void update(String phone, LocalDate dateOfBirth, Instant now) {
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.updatedAt = now;
    }

    public UUID getUserId() { return userId; }
    public String getPhone() { return phone; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}