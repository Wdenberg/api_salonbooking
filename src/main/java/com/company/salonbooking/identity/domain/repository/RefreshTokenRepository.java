package com.company.salonbooking.identity.domain.repository;

import com.company.salonbooking.identity.domain.model.RefreshToken;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    RefreshToken save(RefreshToken refreshToken);

    void deleteExpired(Instant now);

    int revokeAllByUserId(UUID userId, Instant now, String reason);
}