package com.company.salonbooking.identity.infrastructure.persistence;

import com.company.salonbooking.identity.domain.model.RefreshToken;
import com.company.salonbooking.identity.domain.repository.RefreshTokenRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpaRepository;
    private final RefreshTokenMapper mapper;

    public RefreshTokenRepositoryAdapter(RefreshTokenJpaRepository jpaRepository, RefreshTokenMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash)
                .map(mapper::toDomain);
    }

    @Override
    public List<RefreshToken> findAllActive() {
        return jpaRepository.findByRevokedFalse().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenJpaEntity entity = mapper.toEntity(refreshToken);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public void deleteExpired(Instant now) {
        jpaRepository.deleteByExpiresAtBeforeAndRevokedFalse(now);
    }

    @Override
    public int revokeAllByUserId(UUID userId, Instant now, String reason) {
        return jpaRepository.revokeAllByUserId(userId, now, reason);
    }
}