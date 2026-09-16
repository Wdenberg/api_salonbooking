package com.company.salonbooking.employee.interfaces.rest.dto;

import com.company.salonbooking.employee.domain.model.AvailabilityBlock;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record AvailabilityBlockResponse(
        @Schema(description = "Availability block ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
        @Schema(description = "Employee ID", example = "550e8400-e29b-41d4-a716-446655440001")
        UUID employeeId,
        @Schema(description = "Block start time", example = "2026-09-20T10:00:00Z")
        Instant startAt,
        @Schema(description = "Block end time", example = "2026-09-20T12:00:00Z")
        Instant endAt,
        @Schema(description = "Reason for the availability block", example = "Lunch break")
        String reason
) {
    public static AvailabilityBlockResponse from(AvailabilityBlock block) {
        return new AvailabilityBlockResponse(block.getId(), block.getEmployeeId(), block.getStartAt(),
                block.getEndAt(), block.getReason());
    }
}
