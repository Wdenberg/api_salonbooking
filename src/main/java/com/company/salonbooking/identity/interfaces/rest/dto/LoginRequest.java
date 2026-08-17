package com.company.salonbooking.identity.interfaces.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "email is required") @Email String email,
        @NotBlank(message = "password is required") String password
) {}