package com.company.salonbooking.business.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BusinessSettingsJpaRepository extends JpaRepository<BusinessSettingsJpaEntity, UUID> {
}