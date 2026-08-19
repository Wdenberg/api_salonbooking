package com.company.salonbooking.business.domain.model;

import java.time.LocalTime;
import java.util.Objects;

public record TimeRange(LocalTime start, LocalTime end) {

    public TimeRange{
        Objects.requireNonNull(start, "Start must not be null");
        Objects.requireNonNull(end, "End must not be null");
        if(!start.isBefore(end)){
            throw new IllegalArgumentException("start time must be before end time");
        }

    }
    public boolean overlaps(TimeRange other) {
        return start.isBefore(other.end) && other.start.isBefore(end);
    }
}
