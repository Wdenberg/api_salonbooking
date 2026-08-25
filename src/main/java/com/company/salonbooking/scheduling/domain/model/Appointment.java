package com.company.salonbooking.scheduling.domain.model;

import com.company.salonbooking.scheduling.domain.exception.CancellationNotAllowedException;
import com.company.salonbooking.scheduling.domain.exception.InvalidAppointmentTransitionException;
import com.company.salonbooking.shared.domain.model.Money;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Scheduling's main aggregate root (Seção 22). Centralizes ALL state transition rules
 * (Seção 23) so they never leak into controllers or use cases. Overlap prevention is
 * intentionally NOT enforced here — that is the database's job via the exclusion
 * constraint (Seção 24); this class only enforces rules that are checkable in memory.
 */
public final class Appointment {

    private final UUID id;
    private final UUID businessId;
    private final UUID customerId;
    private final UUID employeeId;
    private final UUID serviceId;
    private final Instant startAt;
    private final Instant endAt;
    private AppointmentStatus status;
    private String notes;

    // Historical snapshots (Seção 128-131) — never re-read from Catalog/Employee after creation.
    private final String serviceNameSnapshot;
    private final Money servicePriceSnapshot;
    private final int serviceDurationMinutesSnapshot;
    private final String employeeNameSnapshot;

    private final Instant createdAt;
    private Instant updatedAt;

    private Appointment(UUID id, UUID businessId, UUID customerId, UUID employeeId, UUID serviceId,
                        Instant startAt, Instant endAt, AppointmentStatus status, String notes,
                        String serviceNameSnapshot, Money servicePriceSnapshot, int serviceDurationMinutesSnapshot,
                        String employeeNameSnapshot, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.businessId = Objects.requireNonNull(businessId);
        this.customerId = Objects.requireNonNull(customerId);
        this.employeeId = Objects.requireNonNull(employeeId);
        this.serviceId = Objects.requireNonNull(serviceId);
        this.startAt = Objects.requireNonNull(startAt);
        this.endAt = Objects.requireNonNull(endAt);
        if (!startAt.isBefore(endAt)) {
            throw new IllegalArgumentException("startAt must be before endAt");
        }
        this.status = Objects.requireNonNull(status);
        this.notes = notes;
        this.serviceNameSnapshot = Objects.requireNonNull(serviceNameSnapshot);
        this.servicePriceSnapshot = Objects.requireNonNull(servicePriceSnapshot);
        this.serviceDurationMinutesSnapshot = serviceDurationMinutesSnapshot;
        this.employeeNameSnapshot = Objects.requireNonNull(employeeNameSnapshot);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Appointment schedule(UUID id, UUID businessId, UUID customerId, UUID employeeId, UUID serviceId,
                                       Instant startAt, Instant endAt, String notes,
                                       String serviceNameSnapshot, Money servicePriceSnapshot,
                                       int serviceDurationMinutesSnapshot, String employeeNameSnapshot, Instant now) {
        return new Appointment(id, businessId, customerId, employeeId, serviceId, startAt, endAt,
                AppointmentStatus.PENDING, notes, serviceNameSnapshot, servicePriceSnapshot,
                serviceDurationMinutesSnapshot, employeeNameSnapshot, now, now);
    }

    public static Appointment restore(UUID id, UUID businessId, UUID customerId, UUID employeeId, UUID serviceId,
                                      Instant startAt, Instant endAt, AppointmentStatus status, String notes,
                                      String serviceNameSnapshot, Money servicePriceSnapshot,
                                      int serviceDurationMinutesSnapshot, String employeeNameSnapshot,
                                      Instant createdAt, Instant updatedAt) {
        return new Appointment(id, businessId, customerId, employeeId, serviceId, startAt, endAt, status, notes,
                serviceNameSnapshot, servicePriceSnapshot, serviceDurationMinutesSnapshot, employeeNameSnapshot,
                createdAt, updatedAt);
    }

    /** PENDING -> CONFIRMED */
    public void confirm(Instant now) {
        requireStatus(AppointmentStatus.PENDING, "confirm");
        this.status = AppointmentStatus.CONFIRMED;
        this.updatedAt = now;
    }

    /** PENDING -> CANCELLED or CONFIRMED -> CANCELLED, respecting the cancellation policy (Seção 87, rule 16). */
    public void cancel(Instant now, long cancellationMinimumMinutes) {
        if (status != AppointmentStatus.PENDING && status != AppointmentStatus.CONFIRMED) {
            throw new InvalidAppointmentTransitionException(
                    "Cannot cancel an appointment in status " + status);
        }

        Duration untilStart = Duration.between(now, startAt);
        if (untilStart.toMinutes() < cancellationMinimumMinutes) {
            throw new CancellationNotAllowedException(
                    "Cancellation window has passed. Minimum notice required: " + cancellationMinimumMinutes + " minutes.");
        }

        this.status = AppointmentStatus.CANCELLED;
        this.updatedAt = now;
    }

    /** CONFIRMED -> COMPLETED */
    public void complete(Instant now) {
        requireStatus(AppointmentStatus.CONFIRMED, "complete");
        this.status = AppointmentStatus.COMPLETED;
        this.updatedAt = now;
    }

    /** CONFIRMED -> NO_SHOW */
    public void markNoShow(Instant now) {
        requireStatus(AppointmentStatus.CONFIRMED, "mark as no-show");
        this.status = AppointmentStatus.NO_SHOW;
        this.updatedAt = now;
    }

    private void requireStatus(AppointmentStatus required, String action) {
        if (status != required) {
            throw new InvalidAppointmentTransitionException(
                    "Cannot " + action + " an appointment in status " + status + " (expected " + required + ")");
        }
    }

    public boolean isOwnedByCustomer(UUID customerIdToCheck) {
        return customerId.equals(customerIdToCheck);
    }

    public boolean belongsToBusiness(UUID businessIdToCheck) {
        return businessId.equals(businessIdToCheck);
    }

    public boolean isAssignedEmployee(UUID employeeIdToCheck) {
        return employeeId.equals(employeeIdToCheck);
    }

    public UUID getId() { return id; }
    public UUID getBusinessId() { return businessId; }
    public UUID getCustomerId() { return customerId; }
    public UUID getEmployeeId() { return employeeId; }
    public UUID getServiceId() { return serviceId; }
    public Instant getStartAt() { return startAt; }
    public Instant getEndAt() { return endAt; }
    public AppointmentStatus getStatus() { return status; }
    public String getNotes() { return notes; }
    public String getServiceNameSnapshot() { return serviceNameSnapshot; }
    public Money getServicePriceSnapshot() { return servicePriceSnapshot; }
    public int getServiceDurationMinutesSnapshot() { return serviceDurationMinutesSnapshot; }
    public String getEmployeeNameSnapshot() { return employeeNameSnapshot; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Appointment that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}