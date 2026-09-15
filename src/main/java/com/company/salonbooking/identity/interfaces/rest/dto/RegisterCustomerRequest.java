package com.company.salonbooking.identity.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterCustomerRequest(
        @Schema(description = "Customer full name", example = "Jane Smith")
        @NotBlank(message = "name is required") @Size(max = 150) String name,
        @Schema(description = "Customer email address", example = "customer@example.com")
        @NotBlank(message = "email is required") @Email String email,
        @Schema(description = "Customer password (min 8 characters)", example = "password123")
        @NotBlank(message = "password is required") @Size(min = 8, max = 100) String password
) {}
