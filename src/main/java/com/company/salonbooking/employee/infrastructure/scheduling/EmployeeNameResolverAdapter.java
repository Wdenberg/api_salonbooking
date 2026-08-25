package com.company.salonbooking.employee.infrastructure.scheduling;

import com.company.salonbooking.employee.domain.exception.EmployeeNotFoundException;
import com.company.salonbooking.employee.domain.model.Employee;
import com.company.salonbooking.employee.domain.repository.EmployeeRepository;
import com.company.salonbooking.identity.domain.model.User;
import com.company.salonbooking.identity.domain.repository.UserRepository;
import com.company.salonbooking.scheduling.application.port.EmployeeNameResolver;
import com.company.salonbooking.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Cross-module read (employee + identity) needed to build an Appointment's historical
 * snapshot. Consistent with the existing pattern in employee/application/usecase/*
 * (e.g. CreateEmployeeUseCase), which already depends directly on identity's domain
 * repositories rather than going through an extra abstraction for a same-transaction read.
 */
@Component
public class EmployeeNameResolverAdapter implements EmployeeNameResolver {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    public EmployeeNameResolverAdapter(EmployeeRepository employeeRepository, UserRepository userRepository) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
    }

    @Override
    public String resolveName(UUID employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        User user = userRepository.findById(employee.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found for employee: " + employeeId));

        return user.getName();
    }
}