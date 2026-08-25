package com.company.salonbooking.catalog.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ServiceOfferingJpaRepository extends JpaRepository<ServiceOfferingJpaEntity, UUID> {

    List<ServiceOfferingJpaEntity> findByBusinessId(UUID businessId, Pageable pageable);

    List<ServiceOfferingJpaEntity> findByBusinessIdAndActiveTrue(UUID businessId, Pageable pageable);
}