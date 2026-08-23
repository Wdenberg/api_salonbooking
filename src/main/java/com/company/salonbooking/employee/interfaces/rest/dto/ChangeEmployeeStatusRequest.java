package com.company.salonbooking.employee.interfaces.rest.dto;

import com.company.salonbooking.employee.domain.model.EmployeeStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeEmployeeStatusRequest(@NotNull EmployeeStatus status) {}