package com.company.salonbooking.employee.infrastructure.persistence;

import com.company.salonbooking.employee.domain.model.EmployeeStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeJpaRepository extends JpaRepository<EmployeeJpaEntity, UUID> {

    Optional<EmployeeJpaEntity> findByUserId(UUID userId);

    List<EmployeeJpaEntity> findByBusinessId(UUID businessId, Pageable pageable);

    List<EmployeeJpaEntity> findByBusinessIdAndStatus(UUID businessId, EmployeeStatus status);

    boolean existsByUserIdAndBusinessId(UUID userId, UUID businessId);
}