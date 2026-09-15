package com.company.salonbooking.identity.infrastructure.persistence;

import com.company.salonbooking.identity.domain.model.RefreshToken;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class RefreshTokenMapper {

    public RefreshTokenJpaEntity toEntity(RefreshToken domain) {
        RefreshTokenJpaEntity entity = new RefreshTokenJpaEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setTokenHash(domain.getTokenHash());
        entity.setParentTokenHash(domain.getParentTokenHash());
        entity.setRevoked(domain.getRevoked());
        entity.setRevokedAt(domain.getRevokedAt());
        entity.setRevokedReason(domain.getRevokedReason());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    public RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        return RefreshToken.restore(
                entity.getId(),
                entity.getUserId(),
                entity.getTokenHash(),
                entity.getParentTokenHash(),
                entity.isRevoked(),
                entity.getRevokedAt(),
                entity.getRevokedReason(),
                entity.getExpiresAt(),
                entity.getCreatedAt()
        );
    }
}