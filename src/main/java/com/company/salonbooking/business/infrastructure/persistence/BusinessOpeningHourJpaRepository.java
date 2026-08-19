package com.company.salonbooking.business.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BusinessOpeningHourJpaRepository extends JpaRepository<BusinessOpeningHourJpaEntity, UUID> {

    List<BusinessOpeningHourJpaEntity> findByBusinessId(UUID businessId);

    @Modifying
    @Query("DELETE FROM BusinessOpeningHourJpaEntity e WHERE e.businessId = :businessId")
    void deleteByBusinessId(@Param("businessId") UUID businessId);
}