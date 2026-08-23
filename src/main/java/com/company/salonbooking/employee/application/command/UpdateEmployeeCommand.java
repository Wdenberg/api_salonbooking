package com.company.salonbooking.employee.application.command;

import java.util.UUID;

public record UpdateEmployeeCommand(UUID employeeId, UUID requesterId, String specialty) {}