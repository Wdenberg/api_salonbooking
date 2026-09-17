package com.company.salonbooking.identity.infrastructure.persistence;

import com.company.salonbooking.identity.infrastructure.persistence.RefreshTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, UUID> {

    Optional<RefreshTokenJpaEntity> findByTokenHash(String tokenHash);

    List<RefreshTokenJpaEntity> findByRevokedFalse();

    void deleteByExpiresAtBeforeAndRevokedFalse(Instant now);

    @Modifying
    @Transactional
    @Query("""
        UPDATE RefreshTokenJpaEntity r
        SET r.revoked = true, r.revokedAt = :now, r.revokedReason = :reason
        WHERE r.userId = :userId AND r.revoked = false
        """)
    int revokeAllByUserId(UUID userId, Instant now, String reason);
}