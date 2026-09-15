package com.company.salonbooking.scheduling.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AppointmentJpaRepository extends JpaRepository<AppointmentJpaEntity, UUID>,
        JpaSpecificationExecutor<AppointmentJpaEntity> {

    @Query("SELECT a FROM AppointmentJpaEntity a WHERE a.employeeId = :employeeId " +
            "AND a.status IN ('PENDING','CONFIRMED') AND a.startAt < :to AND a.endAt > :from")
    List<AppointmentJpaEntity> findActiveByEmployeeAndRange(@Param("employeeId") UUID employeeId,
                                                            @Param("from") Instant from, @Param("to") Instant to);

    @Query("SELECT COUNT(a) > 0 FROM AppointmentJpaEntity a WHERE a.employeeId = :employeeId " +
            "AND a.status IN ('PENDING','CONFIRMED') AND a.startAt < :endAt AND a.endAt > :startAt")
    boolean existsOverlapping(@Param("employeeId") UUID employeeId, @Param("startAt") Instant startAt, @Param("endAt") Instant endAt);

    @Query("SELECT a FROM AppointmentJpaEntity a WHERE a.status = 'CONFIRMED' AND a.startAt BETWEEN :from AND :to")
    List<AppointmentJpaEntity> findConfirmedStartingBetween(@Param("from") Instant from, @Param("to") Instant to);

    @Query("SELECT COUNT(a) FROM AppointmentJpaEntity a WHERE a.businessId = :businessId AND a.status IN ('PENDING','CONFIRMED')")
    long countActiveByBusinessId(@Param("businessId") UUID businessId);

    @Query("SELECT COUNT(a) FROM AppointmentJpaEntity a WHERE a.employeeId = :employeeId AND a.status IN ('PENDING','CONFIRMED')")
    long countActiveByEmployeeId(@Param("employeeId") UUID employeeId);

    @Query("SELECT COUNT(a) FROM AppointmentJpaEntity a WHERE a.serviceId = :serviceId AND a.status IN ('PENDING','CONFIRMED')")
    long countActiveByServiceId(@Param("serviceId") UUID serviceId);
}
