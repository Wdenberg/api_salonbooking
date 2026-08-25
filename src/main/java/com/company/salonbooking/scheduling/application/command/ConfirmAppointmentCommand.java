package com.company.salonbooking.scheduling.application.command;

import java.util.UUID;

public record ConfirmAppointmentCommand(UUID appointmentId, UUID requesterId) {}