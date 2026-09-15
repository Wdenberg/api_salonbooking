package com.company.salonbooking.business.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BusinessJpaRepository extends JpaRepository<BusinessJpaEntity, UUID> {

    Optional<BusinessJpaEntity> findByOwnerId(UUID ownerId);
}
