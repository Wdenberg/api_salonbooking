package com.company.salonbooking.identity.application.dto;

import java.util.UUID;

public record AuthResult(UUID userId, String accessToken, long expiresInSeconds) {
}
