package com.company.salonbooking.employee.application.command;

import java.time.Instant;
import java.util.UUID;

public record CreateAvailabilityBlockCommand(UUID employeeId, UUID requesterId, Instant startAt, Instant endAt, String reason) {}