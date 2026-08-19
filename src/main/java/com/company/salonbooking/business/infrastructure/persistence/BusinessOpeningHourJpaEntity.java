package com.company.salonbooking.business.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "business_opening_hours")
public class BusinessOpeningHourJpaEntity {

    @Id
    private UUID id;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;

    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    protected BusinessOpeningHourJpaEntity() {
    }

    public BusinessOpeningHourJpaEntity(UUID id, UUID businessId, DayOfWeek dayOfWeek, LocalTime openTime, LocalTime closeTime) {
        this.id = id;
        this.businessId = businessId;
        this.dayOfWeek = dayOfWeek;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    public UUID getId() { return id; }
    public UUID getBusinessId() { return businessId; }
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public LocalTime getOpenTime() { return openTime; }
    public LocalTime getCloseTime() { return closeTime; }
}