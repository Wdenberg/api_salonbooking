package com.company.salonbooking.scheduling.application.command;

import java.time.Instant;
import java.util.UUID;

public record CreateAppointmentCommand(
        UUID requesterId, UUID businessId, UUID employeeId, UUID serviceId, Instant startAt, String notes
) {}