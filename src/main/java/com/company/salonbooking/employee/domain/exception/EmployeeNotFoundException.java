package com.company.salonbooking.employee.domain.exception;

import com.company.salonbooking.shared.exception.ResourceNotFoundException;

import java.util.UUID;

public class EmployeeNotFoundException extends ResourceNotFoundException {
    public EmployeeNotFoundException(UUID id) {
        super("Employee not found: " + id);
    }
}
