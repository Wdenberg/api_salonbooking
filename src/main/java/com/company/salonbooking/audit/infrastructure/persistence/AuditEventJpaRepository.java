package com.company.salonbooking.audit.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditEventJpaRepository extends JpaRepository<AuditEventJpaEntity, UUID> {

    List<AuditEventJpaEntity> findByBusinessIdOrderByOccurredAtDesc(UUID businessId, Pageable pageable);
}
