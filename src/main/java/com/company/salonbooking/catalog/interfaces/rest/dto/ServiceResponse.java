package com.company.salonbooking.catalog.interfaces.rest.dto;

import com.company.salonbooking.catalog.domain.model.ServiceOffering;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ServiceResponse(
        @Schema(description = "Service ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
        @Schema(description = "Business ID", example = "550e8400-e29b-41d4-a716-446655440001")
        UUID businessId,
        @Schema(description = "Service name", example = "Haircut")
        String name,
        @Schema(description = "Service description", example = "Classic haircut with styling")
        String description,
        @Schema(description = "Service price amount", example = "50.00")
        BigDecimal priceAmount,
        @Schema(description = "ISO 4217 currency code", example = "BRL")
        String priceCurrency,
        @Schema(description = "Service duration in minutes", example = "30")
        int durationMinutes,
        @Schema(description = "Whether the service is active", example = "true")
        boolean active,
        @Schema(description = "Creation timestamp", example = "2026-09-16T10:00:00Z")
        Instant createdAt,
        @Schema(description = "Last update timestamp", example = "2026-09-16T10:00:00Z")
        Instant updatedAt
) {
    public static ServiceResponse from(ServiceOffering service) {
        return new ServiceResponse(service.getId(), service.getBusinessId(), service.getName(), service.getDescription(),
                service.getPrice().getAmount(), service.getPrice().getCurrencyCode(),
                service.getDuration().toMinutes(), service.isActive(), service.getCreatedAt(), service.getUpdatedAt());
    }
}
