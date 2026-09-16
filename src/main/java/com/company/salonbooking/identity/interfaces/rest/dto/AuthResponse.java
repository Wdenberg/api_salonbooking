package com.company.salonbooking.identity.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record AuthResponse(
        @Schema(description = "Authenticated user ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID userId,
        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,
        @Schema(description = "Refresh token for obtaining new access tokens", example = "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...")
        String refreshToken,
        @Schema(description = "Token type", example = "Bearer")
        String tokenType,
        @Schema(description = "Access token expiration time in seconds", example = "3600")
        long expiresIn
) {

    public static AuthResponse of(UUID userId, String accessToken, String refreshToken, long expiresIn) {
        return new AuthResponse(userId, accessToken, refreshToken, "Bearer", expiresIn);
    }
}