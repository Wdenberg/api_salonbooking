package com.company.salonbooking.catalog.interfaces.rest.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record UpdateServiceRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 1000) String description,
        @NotNull @PositiveOrZero BigDecimal priceAmount,
        @NotBlank @Size(min = 3, max = 3) String priceCurrency,
        @Positive int durationMinutes
) {}