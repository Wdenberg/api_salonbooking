package com.company.salonbooking.identity.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterOwnerRequest(
        @Schema(description = "Owner full name", example = "John Doe")
        @NotBlank(message = "name is required") @Size(max = 150) String name,
        @Schema(description = "Owner email address", example = "owner@example.com")
        @NotBlank(message = "email is required") @Email String email,
        @Schema(description = "Owner password (min 8 characters)", example = "password123")
        @NotBlank(message = "password is required") @Size(min = 8, max = 100) String password
) {}
