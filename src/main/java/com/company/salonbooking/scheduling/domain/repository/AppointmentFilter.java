package com.company.salonbooking.scheduling.domain.repository;

import com.company.salonbooking.scheduling.domain.model.AppointmentStatus;

import java.time.Instant;
import java.util.UUID;

/** Optional filter fields, all nullable (Seção 45). */
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