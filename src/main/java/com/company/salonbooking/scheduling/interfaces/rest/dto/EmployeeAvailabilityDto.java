package com.company.salonbooking.scheduling.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

public record EmployeeAvailabilityDto(
        @Schema(description = "Employee ID", example = "550e8400-e29b-41d4-a716-446655440003")
        UUID employeeId,
        @Schema(description = "Available time slots for this employee")
        List<TimeSlotDto> slots
) {}
