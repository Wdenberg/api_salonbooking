package com.company.salonbooking.scheduling.interfaces.rest.dto;

import com.company.salonbooking.scheduling.domain.model.Appointment;
import com.company.salonbooking.scheduling.domain.model.AppointmentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AppointmentResponse(
        UUID id, UUID businessId, UUID customerId, UUID employeeId, UUID serviceId,
        Instant startAt, Instant endAt, AppointmentStatus status, String notes,
        String serviceNameSnapshot, BigDecimal servicePriceAmountSnapshot, String servicePriceCurrencySnapshot,
        int serviceDurationMinutesSnapshot, String employeeNameSnapshot,
        Instant createdAt, Instant updatedAt
) {
    public static AppointmentResponse from(Appointment a) {
        return new AppointmentResponse(a.getId(), a.getBusinessId(), a.getCustomerId(), a.getEmployeeId(), a.getServiceId(),
                a.getStartAt(), a.getEndAt(), a.getStatus(), a.getNotes(), a.getServiceNameSnapshot(),
                a.getServicePriceSnapshot().getAmount(), a.getServicePriceSnapshot().getCurrencyCode(),
                a.getServiceDurationMinutesSnapshot(), a.getEmployeeNameSnapshot(), a.getCreatedAt(), a.getUpdatedAt());
    }
}