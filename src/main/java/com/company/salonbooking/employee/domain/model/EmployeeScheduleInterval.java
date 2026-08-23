package com.company.salonbooking.employee.domain.model;

import com.company.salonbooking.shared.domain.model.TimeRange;

import java.time.DayOfWeek;
import java.util.Objects;
import java.util.UUID;

public final class EmployeeScheduleInterval {

    private final UUID id;
    private final DayOfWeek dayOfWeek;
    private final TimeRange timeRange;

    public EmployeeScheduleInterval(UUID id, DayOfWeek dayOfWeek, TimeRange timeRange) {
        this.id = Objects.requireNonNull(id);
        this.dayOfWeek = Objects.requireNonNull(dayOfWeek);
        this.timeRange = Objects.requireNonNull(timeRange);
    }

    public UUID getId() { return id; }
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public TimeRange getTimeRange() { return timeRange; }
}