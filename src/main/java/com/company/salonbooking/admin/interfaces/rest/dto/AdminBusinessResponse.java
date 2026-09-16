package com.company.salonbooking.admin.interfaces.rest.dto;

import com.company.salonbooking.business.domain.model.Business;
import com.company.salonbooking.business.domain.model.BusinessStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record AdminBusinessResponse(
        @Schema(description = "Business ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
        @Schema(description = "Owner user ID", example = "550e8400-e29b-41d4-a716-446655440001")
        UUID ownerId,
        @Schema(description = "Owner email", example = "owner@example.com")
        String ownerEmail,
        @Schema(description = "Business name", example = "Barbearia do João")
        String name,
        @Schema(description = "Business description", example = "Traditional barbershop since 1990")
        String description,
        @Schema(description = "Business phone number", example = "+55 11 99999-9999")
        String phone,
        @Schema(description = "Business email", example = "contato@barbeariadojoao.com")
        String email,
        @Schema(description = "IANA timezone identifier", example = "America/Sao_Paulo")
        String timezone,
        @Schema(description = "Business status", example = "ACTIVE")
        BusinessStatus status,
        @Schema(description = "Creation timestamp", example = "2026-01-15T10:30:00Z")
        Instant createdAt,
        @Schema(description = "Last update timestamp", example = "2026-06-20T14:45:00Z")
        Instant updatedAt
) {
    public static AdminBusinessResponse from(Business business) {
        return new AdminBusinessResponse(
                business.getId(),
                business.getOwnerId(),
                null, // ownerEmail - would need join
                business.getName(),
                business.getDescription(),
                business.getPhone(),
                business.getEmail(),
                business.getTimezone().getId(),
                business.getStatus(),
                business.getCreatedAt(),
                business.getUpdatedAt()
        );
    }
}