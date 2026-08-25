package com.company.salonbooking.catalog.domain.model;

import java.time.Duration;

/**
 * Named ServiceDuration (rather than raw java.time.Duration) to keep a domain-specific
 * type at the aggregate boundary and enforce the "must be positive, in whole minutes" rule
 * that plain Duration does not carry on its own.
 */
public final class ServiceDuration {

    private final int minutes;

    private ServiceDuration(int minutes) {
        if (minutes <= 0) {
            throw new IllegalArgumentException("duration must be positive");
        }
        this.minutes = minutes;
    }

    public static ServiceDuration ofMinutes(int minutes) {
        return new ServiceDuration(minutes);
    }

    public int toMinutes() {
        return minutes;
    }

    public Duration asJavaDuration() {
        return Duration.ofMinutes(minutes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceDuration that)) return false;
        return minutes == that.minutes;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(minutes);
    }

    @Override
    public String toString() {
        return minutes + "min";
    }
}