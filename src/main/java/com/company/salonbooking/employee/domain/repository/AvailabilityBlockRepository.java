package com.company.salonbooking.employee.domain.repository;

import com.company.salonbooking.employee.domain.model.AvailabilityBlock;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AvailabilityBlockRepository {

    Optional<AvailabilityBlock> findById(UUID id);

    List<AvailabilityBlock> findByEmployeeIdAndRange(UUID employeeId, Instant from, Instant to);

    AvailabilityBlock save(AvailabilityBlock block);

    void deleteById(UUID id);
}