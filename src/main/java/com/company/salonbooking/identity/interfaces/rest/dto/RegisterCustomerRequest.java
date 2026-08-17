package com.company.salonbooking.identity.interfaces.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterCustomerRequest(
        @NotBlank(message = "name is required") @Size(max = 150) String name,
        @NotBlank(message = "email is required") @Email String email,
        @NotBlank(message = "password is required") @Size(min = 8, max = 100) String password
) {}