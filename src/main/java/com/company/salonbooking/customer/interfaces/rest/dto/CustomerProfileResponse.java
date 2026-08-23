package com.company.salonbooking.customer.interfaces.rest.dto;

import com.company.salonbooking.customer.domain.model.CustomerProfile;

import java.time.LocalDate;
import java.util.UUID;

public record CustomerProfileResponse(UUID userId, String phone, LocalDate dateOfBirth) {
    public static CustomerProfileResponse from(CustomerProfile profile) {
        return new CustomerProfileResponse(profile.getUserId(), profile.getPhone(), profile.getDateOfBirth());
    }
}