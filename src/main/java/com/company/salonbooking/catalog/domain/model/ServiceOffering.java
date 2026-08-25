package com.company.salonbooking.catalog.domain.model;

import com.company.salonbooking.shared.domain.model.Money;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class ServiceOffering {

    private final UUID id;
    private final UUID businessId;
    private String name;
    private String description;
    private Money price;
    private ServiceDuration duration;
    private boolean active;
    private final Instant createdAt;
    private Instant updatedAt;

    private ServiceOffering(UUID id, UUID businessId, String name, String description, Money price,
                            ServiceDuration duration, boolean active, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.businessId = Objects.requireNonNull(businessId);
        this.name = requireNonBlank(name, "name");
        this.description = description;
        this.price = Objects.requireNonNull(price, "price must not be null");
        this.duration = Objects.requireNonNull(duration, "duration must not be null");
        this.active = active;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static ServiceOffering create(UUID id, UUID businessId, String name, String description,
                                         Money price, ServiceDuration duration, Instant now) {
        return new ServiceOffering(id, businessId, name, description, price, duration, true, now, now);
    }

    public static ServiceOffering restore(UUID id, UUID businessId, String name, String description, Money price,
                                          ServiceDuration duration, boolean active, Instant createdAt, Instant updatedAt) {
        return new ServiceOffering(id, businessId, name, description, price, duration, active, createdAt, updatedAt);
    }

    public void update(String name, String description, Money price, ServiceDuration duration, Instant now) {
        this.name = requireNonBlank(name, "name");
        this.description = description;
        this.price = Objects.requireNonNull(price, "price must not be null");
        this.duration = Objects.requireNonNull(duration, "duration must not be null");
        this.updatedAt = now;
    }

    public void activate(Instant now) {
        this.active = true;
        this.updatedAt = now;
    }

    public void deactivate(Instant now) {
        this.active = false;
        this.updatedAt = now;
    }

    public boolean belongsToBusiness(UUID businessIdToCheck) {
        return businessId.equals(businessIdToCheck);
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }

    public UUID getId() { return id; }
    public UUID getBusinessId() { return businessId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Money getPrice() { return price; }
    public ServiceDuration getDuration() { return duration; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceOffering that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}