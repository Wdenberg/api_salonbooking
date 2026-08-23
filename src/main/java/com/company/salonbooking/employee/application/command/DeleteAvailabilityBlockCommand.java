package com.company.salonbooking.employee.application.command;

import java.util.UUID;

public record DeleteAvailabilityBlockCommand(UUID blockId, UUID requesterId) {}