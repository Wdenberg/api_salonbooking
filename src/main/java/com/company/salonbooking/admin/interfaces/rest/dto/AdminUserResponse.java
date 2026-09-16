package com.company.salonbooking.admin.interfaces.rest.dto;

import com.company.salonbooking.identity.domain.model.User;
import com.company.salonbooking.identity.domain.model.UserStatus;
import com.company.salonbooking.identity.domain.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AdminUserResponse(
        @Schema(description = "User ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
        @Schema(description = "User full name", example = "John Doe")
        String name,
        @Schema(description = "User email", example = "john@example.com")
        String email,
        @Schema(description = "User roles", example = "[\"OWNER\"]")
        Set<Role> roles,
        @Schema(description = "User status", example = "ACTIVE")
        UserStatus status,
        @Schema(description = "Creation timestamp", example = "2026-01-15T10:30:00Z")
        Instant createdAt,
        @Schema(description = "Last update timestamp", example = "2026-06-20T14:45:00Z")
        Instant updatedAt
) {
    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRoles(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}