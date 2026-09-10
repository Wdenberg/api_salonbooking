package com.company.salonbooking.scheduling.domain.model;

import java.time.Instant;
import java.util.UUID;

/** Optional filter fields, all nullable. */
public record AppointmentFilter(
        AppointmentStatus status,
        UUID employeeId,
        UUID serviceId,
        Instant dateFrom,
        Instant dateTo
) {
    public static AppointmentFilter empty() {
        return new AppointmentFilter(null, null, null, null, null);
    }
}
