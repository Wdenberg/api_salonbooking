package com.company.salonbooking.business.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "business_settings")
public class BusinessSettingsJpaEntity {

    @Id
    @Column(name = "business_id")
    private UUID businessId;

    @Column(name = "minimum_advance_minutes", nullable = false)
    private int minimumAdvanceMinutes;

    @Column(name = "maximum_advance_days", nullable = false)
    private int maximumAdvanceDays;

    @Column(name = "cancellation_minimum_minutes", nullable = false)
    private int cancellationMinimumMinutes;

    @Column(name = "slot_interval_minutes", nullable = false)
    private int slotIntervalMinutes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected BusinessSettingsJpaEntity() {
    }

    public BusinessSettingsJpaEntity(UUID businessId, int minimumAdvanceMinutes, int maximumAdvanceDays,
                                     int cancellationMinimumMinutes, int slotIntervalMinutes,
                                     Instant createdAt, Instant updatedAt) {
        this.businessId = businessId;
        this.minimumAdvanceMinutes = minimumAdvanceMinutes;
        this.maximumAdvanceDays = maximumAdvanceDays;
        this.cancellationMinimumMinutes = cancellationMinimumMinutes;
        this.slotIntervalMinutes = slotIntervalMinutes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getBusinessId() { return businessId; }
    public int getMinimumAdvanceMinutes() { return minimumAdvanceMinutes; }
    public int getMaximumAdvanceDays() { return maximumAdvanceDays; }
    public int getCancellationMinimumMinutes() { return cancellationMinimumMinutes; }
    public int getSlotIntervalMinutes() { return slotIntervalMinutes; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}