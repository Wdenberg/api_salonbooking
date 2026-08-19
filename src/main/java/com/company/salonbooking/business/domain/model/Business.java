package com.company.salonbooking.business.domain.model;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

public final class Business {

    private final UUID id;
    private final UUID ownerId;
    private String name;
    private String description;
    private String phone;
    private String email;
    private Address address;
    private final ZoneId timezone;
    private BusinessStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private Business(UUID id, UUID ownerId, String name, String description, String phone, String email,
                     Address address, ZoneId timezone, BusinessStatus status, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.ownerId = Objects.requireNonNull(ownerId);
        this.name = requireNonBlank(name, "name");
        this.description = description;
        this.phone = phone;
        this.email = email;
        this.address = address == null ? Address.empty() : address;
        this.timezone = Objects.requireNonNull(timezone, "timezone must not be null");
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Business create(UUID id, UUID ownerId, String name, String description, String phone,
                                  String email, Address address, ZoneId timezone, Instant now) {
        return new Business(id, ownerId, name, description, phone, email, address, timezone, BusinessStatus.ACTIVE, now, now);
    }

    public static Business restore(UUID id, UUID ownerId, String name, String description, String phone, String email,
                                   Address address, ZoneId timezone, BusinessStatus status, Instant createdAt, Instant updatedAt) {
        return new Business(id, ownerId, name, description, phone, email, address, timezone, status, createdAt, updatedAt);
    }

    public void update(String name, String description, String phone, String email, Address address, Instant now) {
        this.name = requireNonBlank(name, "name");
        this.description = description;
        this.phone = phone;
        this.email = email;
        this.address = address == null ? Address.empty() : address;
        this.updatedAt = now;
    }

    public void changeStatus(BusinessStatus newStatus, Instant now) {
        this.status = Objects.requireNonNull(newStatus);
        this.updatedAt = now;
    }

    public boolean isOwnedBy(UUID userId) {
        return ownerId.equals(userId);
    }

    public boolean isActive() {
        return status == BusinessStatus.ACTIVE;
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }

    public UUID getId() { return id; }
    public UUID getOwnerId() { return ownerId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public Address getAddress() { return address; }
    public ZoneId getTimezone() { return timezone; }
    public BusinessStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Business business)) return false;
        return id.equals(business.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
