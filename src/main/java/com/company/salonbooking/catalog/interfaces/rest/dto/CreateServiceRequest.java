package com.company.salonbooking.catalog.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateServiceRequest(
        @Schema(description = "Service name", example = "Haircut")
        @NotBlank @Size(max = 150) String name,
        @Schema(description = "Service description", example = "Classic haircut with styling")
        @Size(max = 1000) String description,
        @Schema(description = "Service price amount", example = "50.00")
        @NotNull @PositiveOrZero BigDecimal priceAmount,
        @Schema(description = "ISO 4217 currency code", example = "BRL")
        @NotBlank @Size(min = 3, max = 3) String priceCurrency,
        @Schema(description = "Service duration in minutes", example = "30")
        @Positive int durationMinutes
) {}
