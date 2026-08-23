package com.company.salonbooking.employee.interfaces.rest.dto;

import com.company.salonbooking.employee.domain.model.Employee;
import com.company.salonbooking.employee.domain.model.EmployeeStatus;

import java.time.Instant;
import java.util.UUID;

public record EmployeeResponse(UUID id, UUID userId, UUID businessId, String specialty, EmployeeStatus status,
                               Instant createdAt, Instant updatedAt) {
    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(employee.getId(), employee.getUserId(), employee.getBusinessId(),
                employee.getSpecialty(), employee.getStatus(), employee.getCreatedAt(), employee.getUpdatedAt());
    }
}