package com.company.salonbooking.scheduling.interfaces.rest.dto;

import com.company.salonbooking.scheduling.domain.model.Appointment;
import com.company.salonbooking.scheduling.domain.model.AppointmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AppointmentResponse(
        @Schema(description = "Appointment ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
        @Schema(description = "Business ID", example = "550e8400-e29b-41d4-a716-446655440001")
        UUID businessId,
        @Schema(description = "Customer ID", example = "550e8400-e29b-41d4-a716-446655440002")
        UUID customerId,
        @Schema(description = "Employee ID", example = "550e8400-e29b-41d4-a716-446655440003")
        UUID employeeId,
        @Schema(description = "Service ID", example = "550e8400-e29b-41d4-a716-446655440004")
        UUID serviceId,
        @Schema(description = "Appointment start time", example = "2026-09-20T14:00:00Z")
        Instant startAt,
        @Schema(description = "Appointment end time", example = "2026-09-20T14:30:00Z")
        Instant endAt,
        @Schema(description = "Appointment status", example = "PENDING")
        AppointmentStatus status,
        @Schema(description = "Appointment notes", example = "Please use scissors only")
        String notes,
        @Schema(description = "Service name at time of booking", example = "Haircut")
        String serviceNameSnapshot,
        @Schema(description = "Service price amount at time of booking", example = "50.00")
        BigDecimal servicePriceAmountSnapshot,
        @Schema(description = "Service price currency at time of booking", example = "BRL")
        String servicePriceCurrencySnapshot,
        @Schema(description = "Service duration in minutes at time of booking", example = "30")
        int serviceDurationMinutesSnapshot,
        @Schema(description = "Employee name at time of booking", example = "John Smith")
        String employeeNameSnapshot,
        @Schema(description = "Creation timestamp", example = "2026-09-16T10:00:00Z")
        Instant createdAt,
        @Schema(description = "Last update timestamp", example = "2026-09-16T10:00:00Z")
        Instant updatedAt
) {
    public static AppointmentResponse from(Appointment a) {
        return new AppointmentResponse(a.getId(), a.getBusinessId(), a.getCustomerId(), a.getEmployeeId(), a.getServiceId(),
                a.getStartAt(), a.getEndAt(), a.getStatus(), a.getNotes(), a.getServiceNameSnapshot(),
                a.getServicePriceSnapshot().getAmount(), a.getServicePriceSnapshot().getCurrencyCode(),
                a.getServiceDurationMinutesSnapshot(), a.getEmployeeNameSnapshot(), a.getCreatedAt(), a.getUpdatedAt());
    }
}
