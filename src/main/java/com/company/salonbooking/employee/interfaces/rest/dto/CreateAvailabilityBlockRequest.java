package com.company.salonbooking.employee.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateAvailabilityBlockRequest(
        @Schema(description = "Block start time (ISO 8601)", example = "2026-09-20T10:00:00Z")
        @NotNull @Future Instant startAt,
        @Schema(description = "Block end time (ISO 8601)", example = "2026-09-20T12:00:00Z")
        @NotNull Instant endAt,
        @Schema(description = "Reason for the availability block", example = "Lunch break")
        @Size(max = 200) String reason
) {}
