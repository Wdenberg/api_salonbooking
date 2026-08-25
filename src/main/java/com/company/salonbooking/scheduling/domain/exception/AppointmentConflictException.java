package com.company.salonbooking.scheduling.domain.exception;

/**
 * Raised when the database rejects an insert/update due to the exclusion constraint
 * (overlapping appointment for the same employee). This is the domain-level translation
 * of a PostgreSQL exclusion_violation (SQLState 23P01) — see AppointmentRepositoryAdapter.
 */
public class AppointmentConflictException extends RuntimeException {

    public AppointmentConflictException() {
        super("The selected time slot is no longer available.");
    }
}