package com.company.salonbooking.scheduling.domain.model;

import java.time.Instant;
import java.util.Objects;

public record TimeSlot(Instant start, Instant end) {

    public TimeSlot {
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("start must be before end");
        }
    }

    public boolean overlaps(Instant otherStart, Instant otherEnd) {
        return start.isBefore(otherEnd) && otherStart.isBefore(end);
    }
}