package com.company.salonbooking.employee.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateEmployeeRequest(
        @Schema(description = "Employee full name", example = "Maria Silva")
        @NotBlank @Size(max = 150) String name,
        @Schema(description = "Employee email", example = "maria@barbearia.com")
        @NotBlank @Email String email,
        @Schema(description = "Employee password (min 8 characters)", example = "password123")
        @NotBlank @Size(min = 8, max = 100) String password,
        @Schema(description = "Employee specialty/role", example = "Barbeiro")
        @Size(max = 150) String specialty
) {}
