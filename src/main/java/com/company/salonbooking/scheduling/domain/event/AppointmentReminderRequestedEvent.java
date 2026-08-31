package com.company.salonbooking.scheduling.domain.event;

import com.company.salonbooking.shared.domain.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record AppointmentReminderRequestedEvent(
        UUID appointmentId, UUID businessId, UUID customerId, Instant startAt, String reminderType
) implements DomainEvent {

    @Override
    public String eventType() { return "AppointmentReminderRequested"; }

    @Override
    public String aggregateType() { return "Appointment"; }

    @Override
    public UUID aggregateId() { return appointmentId; }
}