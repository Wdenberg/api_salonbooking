package com.company.salonbooking.customer.interfaces.rest.dto;

import com.company.salonbooking.customer.domain.model.CustomerProfile;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

public record CustomerProfileResponse(
        @Schema(description = "User ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID userId,
        @Schema(description = "Phone number", example = "+55 11 99999-9999")
        String phone,
        @Schema(description = "Date of birth", example = "1990-01-15")
        LocalDate dateOfBirth
) {
    public static CustomerProfileResponse from(CustomerProfile profile) {
        return new CustomerProfileResponse(profile.getUserId(), profile.getPhone(), profile.getDateOfBirth());
    }
}
