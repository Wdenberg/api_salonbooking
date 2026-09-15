package com.company.salonbooking.employee.interfaces.rest.dto;

import com.company.salonbooking.employee.domain.model.Employee;
import com.company.salonbooking.employee.domain.model.EmployeeStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record EmployeeResponse(
        @Schema(description = "Employee unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
        @Schema(description = "Associated user ID", example = "550e8400-e29b-41d4-a716-446655440001")
        UUID userId,
        @Schema(description = "Business ID", example = "550e8400-e29b-41d4-a716-446655440002")
        UUID businessId,
        @Schema(description = "Employee specialty/role", example = "Barbeiro")
        String specialty,
        @Schema(description = "Employee status", example = "ACTIVE")
        EmployeeStatus status,
        @Schema(description = "Creation timestamp", example = "2026-01-15T10:30:00Z")
        Instant createdAt,
        @Schema(description = "Last update timestamp", example = "2026-06-20T14:45:00Z")
        Instant updatedAt
) {
    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(employee.getId(), employee.getUserId(), employee.getBusinessId(),
                employee.getSpecialty(), employee.getStatus(), employee.getCreatedAt(), employee.getUpdatedAt());
    }
}
