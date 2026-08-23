package com.company.salonbooking.employee.application.command;

import java.util.UUID;

public record CreateEmployeeCommand(UUID requesterId, UUID businessId, String name, String email,
                                    String rawPassword, String specialty) {}