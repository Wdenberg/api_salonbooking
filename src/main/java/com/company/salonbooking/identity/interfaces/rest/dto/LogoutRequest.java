package com.company.salonbooking.identity.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LogoutRequest(
        @Schema(description = "JWT access token to be revoked (added to blocklist)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,
        @Schema(description = "Refresh token to be revoked", example = "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...")
        String refreshToken
) {}