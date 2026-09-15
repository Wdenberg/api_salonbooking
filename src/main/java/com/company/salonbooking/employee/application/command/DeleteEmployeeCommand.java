package com.company.salonbooking.employee.application.command;

import java.util.UUID;

public record DeleteEmployeeCommand(UUID employeeId, UUID requesterId) {}