package com.company.salonbooking.identity.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(description = "User email address", example = "user@example.com")
        @NotBlank(message = "email is required") @Email String email,
        @Schema(description = "User password", example = "password123")
        @NotBlank(message = "password is required") String password
) {}
