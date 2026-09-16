package com.company.salonbooking.employee.domain.repository;

import com.company.salonbooking.employee.domain.model.AvailabilityBlock;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AvailabilityBlockRepository {

    Optional<AvailabilityBlock> findById(UUID id);

    List<AvailabilityBlock> findByEmployeeIdAndRange(UUID employeeId, Instant from, Instant to);

    /** Finds all availability blocks for an employee (no date filter). */
    List<AvailabilityBlock> findByEmployeeId(UUID employeeId);

    AvailabilityBlock save(AvailabilityBlock block);

    void deleteById(UUID id);
}
