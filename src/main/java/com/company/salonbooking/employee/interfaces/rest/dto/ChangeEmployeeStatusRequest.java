package com.company.salonbooking.employee.interfaces.rest.dto;

import com.company.salonbooking.employee.domain.model.EmployeeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ChangeEmployeeStatusRequest(
        @Schema(description = "Employee status", example = "ACTIVE")
        @NotNull EmployeeStatus status
) {}
