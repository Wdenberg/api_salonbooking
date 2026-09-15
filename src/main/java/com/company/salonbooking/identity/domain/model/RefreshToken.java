package com.company.salonbooking.identity.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class RefreshToken {

    private final UUID id;
    private final UUID userId;
    private final String tokenHash;
    private final String parentTokenHash;
    private boolean revoked;
    private Instant revokedAt;
    private String revokedReason;
    private final Instant expiresAt;
    private final Instant createdAt;

    private RefreshToken(UUID id, UUID userId, String tokenHash, String parentTokenHash,
                         boolean revoked, Instant revokedAt, String revokedReason,
                         Instant expiresAt, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.tokenHash = Objects.requireNonNull(tokenHash, "tokenHash must not be null");
        this.parentTokenHash = parentTokenHash;
        this.revoked = revoked;
        this.revokedAt = revokedAt;
        this.revokedReason = revokedReason;
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    public static RefreshToken create(UUID id, UUID userId, String tokenHash, String parentTokenHash,
                                      Instant expiresAt, Instant now) {
        return new RefreshToken(id, userId, tokenHash, parentTokenHash, false, null, null, expiresAt, now);
    }

    public static RefreshToken restore(UUID id, UUID userId, String tokenHash, String parentTokenHash,
                                       boolean revoked, Instant revokedAt, String revokedReason,
                                       Instant expiresAt, Instant createdAt) {
        return new RefreshToken(id, userId, tokenHash, parentTokenHash, revoked, revokedAt, revokedReason, expiresAt, createdAt);
    }

    public void revoke(String reason, Instant now) {
        this.revoked = true;
        this.revokedAt = now;
        this.revokedReason = reason;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }

    public boolean isValid(Instant now) {
        return !revoked && !isExpired(now);
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getTokenHash() { return tokenHash; }
    public String getParentTokenHash() { return parentTokenHash; }
    public boolean getRevoked() { return revoked; }
    public Instant getRevokedAt() { return revokedAt; }
    public String getRevokedReason() { return revokedReason; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RefreshToken that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}