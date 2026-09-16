package com.company.salonbooking.scheduling.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record CreateAppointmentRequest(
        @Schema(description = "Business ID", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull UUID businessId,
        @Schema(description = "Employee ID", example = "550e8400-e29b-41d4-a716-446655440001")
        @NotNull UUID employeeId,
        @Schema(description = "Service ID", example = "550e8400-e29b-41d4-a716-446655440002")
        @NotNull UUID serviceId,
        @Schema(description = "Appointment start time (ISO 8601)", example = "2026-09-20T14:00:00Z")
        @NotNull @Future Instant startAt,
        @Schema(description = "Optional notes", example = "Please use scissors only")
        @Size(max = 500) String notes
) {}
