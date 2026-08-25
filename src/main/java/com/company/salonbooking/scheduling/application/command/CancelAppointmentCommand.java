package com.company.salonbooking.scheduling.application.command;

import java.util.UUID;

public record CancelAppointmentCommand(UUID appointmentId, UUID requesterId) {}