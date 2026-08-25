package com.company.salonbooking.scheduling.domain.event;

import com.company.salonbooking.shared.domain.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record AppointmentConfirmedEvent(
        UUID appointmentId, UUID businessId, UUID customerId, UUID employeeId, Instant startAt
) implements DomainEvent {

    @Override
    public String eventType() { return "AppointmentConfirmed"; }

    @Override
    public String aggregateType() { return "Appointment"; }

    @Override
    public UUID aggregateId() { return appointmentId; }
}