package com.company.salonbooking.catalog.interfaces.rest.dto;

import com.company.salonbooking.catalog.domain.model.ServiceOffering;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ServiceResponse(UUID id, UUID businessId, String name, String description,
                              BigDecimal priceAmount, String priceCurrency, int durationMinutes,
                              boolean active, Instant createdAt, Instant updatedAt) {
    public static ServiceResponse from(ServiceOffering service) {
        return new ServiceResponse(service.getId(), service.getBusinessId(), service.getName(), service.getDescription(),
                service.getPrice().getAmount(), service.getPrice().getCurrencyCode(),
                service.getDuration().toMinutes(), service.isActive(), service.getCreatedAt(), service.getUpdatedAt());
    }
}