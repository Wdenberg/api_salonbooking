package com.company.salonbooking.business.interfaces.rest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateBusinessRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 1000) String description,
        String phone,
        String email,
        @Valid AddressDto address
) {}