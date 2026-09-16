package com.company.salonbooking.customer.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record UpdateCustomerProfileRequest(
        @Schema(description = "Phone number", example = "+55 11 99999-9999")
        String phone,
        @Schema(description = "Date of birth", example = "1990-01-15")
        LocalDate dateOfBirth
) {}
