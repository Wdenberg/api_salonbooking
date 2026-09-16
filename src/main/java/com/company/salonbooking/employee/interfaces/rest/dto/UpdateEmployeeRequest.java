package com.company.salonbooking.employee.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record UpdateEmployeeRequest(
        @Schema(description = "Employee specialty/role", example = "Barbeiro Sênior")
        @Size(max = 150) String specialty
) {}
