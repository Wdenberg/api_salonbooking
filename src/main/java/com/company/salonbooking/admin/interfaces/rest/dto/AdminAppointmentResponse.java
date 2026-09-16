package com.company.salonbooking.admin.interfaces.rest.dto;

import com.company.salonbooking.scheduling.domain.model.Appointment;
import com.company.salonbooking.scheduling.domain.model.AppointmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AdminAppointmentResponse(
        @Schema(description = "Appointment ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
        @Schema(description = "Business ID", example = "550e8400-e29b-41d4-a716-446655440001")
        UUID businessId,
        @Schema(description = "Business name", example = "Barbearia do João")
        String businessName,
        @Schema(description = "Customer ID", example = "550e8400-e29b-41d4-a716-446655440002")
        UUID customerId,
        @Schema(description = "Customer name", example = "John Doe")
        String customerName,
        @Schema(description = "Customer email", example = "john@example.com")
        String customerEmail,
        @Schema(description = "Employee ID", example = "550e8400-e29b-41d4-a716-446655440003")
        UUID employeeId,
        @Schema(description = "Employee name", example = "Maria Silva")
        String employeeName,
        @Schema(description = "Service ID", example = "550e8400-e29b-41d4-a716-446655440004")
        UUID serviceId,
        @Schema(description = "Service name", example = "Haircut")
        String serviceName,
        @Schema(description = "Appointment start time", example = "2026-09-20T14:00:00Z")
        Instant startAt,
        @Schema(description = "Appointment end time", example = "2026-09-20T14:30:00Z")
        Instant endAt,
        @Schema(description = "Appointment status", example = "PENDING")
        AppointmentStatus status,
        @Schema(description = "Appointment notes", example = "Please use scissors only")
        String notes,
        @Schema(description = "Creation timestamp", example = "2026-09-16T10:00:00Z")
        Instant createdAt,
        @Schema(description = "Last update timestamp", example = "2026-09-16T10:00:00Z")
        Instant updatedAt
) {
    public static AdminAppointmentResponse from(Appointment a) {
        return new AdminAppointmentResponse(
                a.getId(),
                a.getBusinessId(),
                null, // businessName - would need join
                a.getCustomerId(),
                null, // customerName - would need join
                null, // customerEmail - would need join
                a.getEmployeeId(),
                a.getEmployeeNameSnapshot(),
                a.getServiceId(),
                a.getServiceNameSnapshot(),
                a.getStartAt(),
                a.getEndAt(),
                a.getStatus(),
                a.getNotes(),
                a.getCreatedAt(),
                a.getUpdatedAt()
        );
    }
}