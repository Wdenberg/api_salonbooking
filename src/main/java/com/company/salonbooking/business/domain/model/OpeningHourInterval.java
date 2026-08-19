package com.company.salonbooking.business.domain.model;

import java.time.DayOfWeek;
import java.util.Objects;
import java.util.UUID;

public final class OpeningHourInterval {

    private final UUID id;
    private final DayOfWeek dayOfWeek;
    private final TimeRange timeRange;

    public OpeningHourInterval(UUID id, DayOfWeek dayOfWeek, TimeRange timeRange) {
        this.id = Objects.requireNonNull(id);
        this.dayOfWeek = Objects.requireNonNull(dayOfWeek);
        this.timeRange = Objects.requireNonNull(timeRange);
    }

    public UUID getId() { return id; }
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public TimeRange getTimeRange() { return timeRange; }
}
