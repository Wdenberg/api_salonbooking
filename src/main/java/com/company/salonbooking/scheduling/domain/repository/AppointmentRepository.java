package com.company.salonbooking.scheduling.domain.repository;

import com.company.salonbooking.scheduling.domain.model.Appointment;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository {

    Optional<Appointment> findById(UUID id);

    /** Active (PENDING/CONFIRMED) appointments for an employee within a time range — used for availability. */
    List<Appointment> findActiveByEmployeeAndRange(UUID employeeId, Instant from, Instant to);

    /** Defensive pre-check (Seção 24, step 1) before attempting the insert. Not the source of truth. */
    boolean existsOverlapping(UUID employeeId, Instant startAt, Instant endAt);

    /**
     * Persists the appointment. Implementations MUST translate a PostgreSQL exclusion
     * constraint violation into AppointmentConflictException rather than letting a raw
     * persistence exception escape the repository boundary.
     */
    Appointment save(Appointment appointment);

    List<Appointment> findByCustomerId(UUID customerId, AppointmentFilter filter, int page, int size);

    List<Appointment> findByBusinessId(UUID businessId, AppointmentFilter filter, int page, int size);
}