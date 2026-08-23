package com.company.salonbooking.employee.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface EmployeeScheduleJpaRepository extends JpaRepository<EmployeeScheduleJpaEntity, UUID> {

    List<EmployeeScheduleJpaEntity> findByEmployeeId(UUID employeeId);

    @Modifying
    @Query("DELETE FROM EmployeeScheduleJpaEntity e WHERE e.employeeId = :employeeId")
    void deleteByEmployeeId(@Param("employeeId") UUID employeeId);
}