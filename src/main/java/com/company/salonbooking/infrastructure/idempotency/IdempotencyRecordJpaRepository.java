package com.company.salonbooking.infrastructure.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyRecordJpaRepository extends JpaRepository<IdempotencyRecordJpaEntity, UUID> {

    Optional<IdempotencyRecordJpaEntity> findByIdempotencyKeyAndUserIdAndEndpoint(
            String idempotencyKey, UUID userId, String endpoint);
}