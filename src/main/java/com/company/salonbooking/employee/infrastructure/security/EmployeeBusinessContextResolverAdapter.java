package com.company.salonbooking.employee.infrastructure.security;

import com.company.salonbooking.employee.domain.model.Employee;
import com.company.salonbooking.employee.domain.repository.EmployeeRepository;
import com.company.salonbooking.identity.application.port.BusinessContextResolver;
import com.company.salonbooking.identity.domain.model.Role;
import com.company.salonbooking.identity.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class EmployeeBusinessContextResolverAdapter implements BusinessContextResolver {

    private final EmployeeRepository employeeRepository;

    public EmployeeBusinessContextResolverAdapter(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Optional<UUID> resolveBusinessId(User user) {
        if (!user.hasRole(Role.EMPLOYEE)) {
            return Optional.empty();
        }

        return employeeRepository.findByUserId(user.getId())
                .filter(Employee::isActive)
                .map(Employee::getBusinessId);
    }
}