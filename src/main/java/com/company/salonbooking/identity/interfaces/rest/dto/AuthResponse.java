package com.company.salonbooking.identity.interfaces.rest.dto;

import java.util.UUID;

public record AuthResponse(UUID userId, String accessToken, String tokenType, long expiresIn) {

    public static AuthResponse of(UUID userId, String accessToken, long expiresIn) {
        return new AuthResponse(userId, accessToken, "Bearer", expiresIn);
    }
}