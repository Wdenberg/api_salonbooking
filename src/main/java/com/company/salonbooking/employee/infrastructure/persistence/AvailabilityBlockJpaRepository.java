package com.company.salonbooking.employee.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AvailabilityBlockJpaRepository extends JpaRepository<AvailabilityBlockJpaEntity, UUID> {

    @Query("SELECT b FROM AvailabilityBlockJpaEntity b WHERE b.employeeId = :employeeId " +
            "AND b.startAt < :to AND b.endAt > :from")
    List<AvailabilityBlockJpaEntity> findByEmployeeIdAndRange(@Param("employeeId") UUID employeeId,
                                                              @Param("from") Instant from, @Param("to") Instant to);
}