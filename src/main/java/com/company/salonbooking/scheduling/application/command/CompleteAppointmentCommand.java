package com.company.salonbooking.scheduling.application.command;

import java.util.UUID;

public record CompleteAppointmentCommand(UUID appointmentId, UUID requesterId) {}