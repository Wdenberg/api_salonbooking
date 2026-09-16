package com.company.salonbooking.identity.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface AccessTokenBlocklistJpaRepository extends JpaRepository<AccessTokenBlocklistJpaEntity, UUID> {

    Optional<AccessTokenBlocklistJpaEntity> findByTokenJti(String tokenJti);

    void deleteByExpiresAtBefore(Instant now);

    boolean existsByTokenJti(String tokenJti);
}