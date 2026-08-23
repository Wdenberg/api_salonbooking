package com.company.salonbooking.employee.application.command;

import com.company.salonbooking.employee.domain.model.EmployeeStatus;

import java.util.UUID;

public record ChangeEmployeeStatusCommand(UUID employeeId, UUID requesterId, EmployeeStatus newStatus) {}