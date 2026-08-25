package com.company.salonbooking.scheduling.domain.event;

import com.company.salonbooking.shared.domain.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record AppointmentCreatedEvent(
        UUID appointmentId, UUID businessId, UUID customerId, UUID employeeId, UUID serviceId,
        Instant startAt, Instant endAt
) implements DomainEvent {

    @Override
    public String eventType() { return "AppointmentCreated"; }

    @Override
    public String aggregateType() { return "Appointment"; }

    @Override
    public UUID aggregateId() { return appointmentId; }
}