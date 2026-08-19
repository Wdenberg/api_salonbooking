package com.company.salonbooking.business.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class BusinessSettings {

    private final UUID businessId;
    private int minimumAdvanceMinutes;
    private int maximumAdvanceDays;
    private int cancellationMinimumMinutes;
    private int slotIntervalMinutes;
    private final Instant createdAt;
    private Instant updatedAt;

    private BusinessSettings(UUID businessId, int minimumAdvanceMinutes, int maximumAdvanceDays,
                             int cancellationMinimumMinutes, int slotIntervalMinutes,
                             Instant createdAt, Instant updatedAt) {
        this.businessId = Objects.requireNonNull(businessId);
        this.minimumAdvanceMinutes = requireNonNegative(minimumAdvanceMinutes, "minimumAdvanceMinutes");
        this.maximumAdvanceDays = requirePositive(maximumAdvanceDays, "maximumAdvanceDays");
        this.cancellationMinimumMinutes = requireNonNegative(cancellationMinimumMinutes, "cancellationMinimumMinutes");
        this.slotIntervalMinutes = requirePositive(slotIntervalMinutes, "slotIntervalMinutes");
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    /** Sensible defaults applied automatically when a Business is created (Seção 133). */
    public static BusinessSettings defaultsFor(UUID businessId, Instant now) {
        return new BusinessSettings(businessId, 60, 30, 120, 30, now, now);
    }

    public static BusinessSettings restore(UUID businessId, int minimumAdvanceMinutes, int maximumAdvanceDays,
                                           int cancellationMinimumMinutes, int slotIntervalMinutes,
                                           Instant createdAt, Instant updatedAt) {
        return new BusinessSettings(businessId, minimumAdvanceMinutes, maximumAdvanceDays,
                cancellationMinimumMinutes, slotIntervalMinutes, createdAt, updatedAt);
    }

    public void update(int minimumAdvanceMinutes, int maximumAdvanceDays, int cancellationMinimumMinutes,
                       int slotIntervalMinutes, Instant now) {
        this.minimumAdvanceMinutes = requireNonNegative(minimumAdvanceMinutes, "minimumAdvanceMinutes");
        this.maximumAdvanceDays = requirePositive(maximumAdvanceDays, "maximumAdvanceDays");
        this.cancellationMinimumMinutes = requireNonNegative(cancellationMinimumMinutes, "cancellationMinimumMinutes");
        this.slotIntervalMinutes = requirePositive(slotIntervalMinutes, "slotIntervalMinutes");
        this.updatedAt = now;
    }

    private static int requireNonNegative(int value, String field) {
        if (value < 0) throw new IllegalArgumentException(field + " must not be negative");
        return value;
    }

    private static int requirePositive(int value, String field) {
        if (value <= 0) throw new IllegalArgumentException(field + " must be positive");
        return value;
    }

    public UUID getBusinessId() { return businessId; }
    public int getMinimumAdvanceMinutes() { return minimumAdvanceMinutes; }
    public int getMaximumAdvanceDays() { return maximumAdvanceDays; }
    public int getCancellationMinimumMinutes() { return cancellationMinimumMinutes; }
    public int getSlotIntervalMinutes() { return slotIntervalMinutes; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
