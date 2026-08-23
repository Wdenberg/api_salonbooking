package com.company.salonbooking.employee.application.usecase;

import com.company.salonbooking.business.domain.exception.BusinessNotFoundException;
import com.company.salonbooking.business.domain.repository.BusinessRepository;
import com.company.salonbooking.employee.application.command.ChangeEmployeeStatusCommand;
import com.company.salonbooking.employee.domain.exception.EmployeeNotFoundException;
import com.company.salonbooking.employee.domain.model.Employee;
import com.company.salonbooking.employee.domain.repository.EmployeeRepository;
import com.company.salonbooking.shared.exception.UnauthorizedResourceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class ChangeEmployeeStatusUseCase {

    private final EmployeeRepository employeeRepository;
    private final BusinessRepository businessRepository;
    private final Clock clock;

    public ChangeEmployeeStatusUseCase(EmployeeRepository employeeRepository, BusinessRepository businessRepository, Clock clock) {
        this.employeeRepository = employeeRepository;
        this.businessRepository = businessRepository;
        this.clock = clock;
    }

    @Transactional
    public Employee execute(ChangeEmployeeStatusCommand command) {
        Employee employee = employeeRepository.findById(command.employeeId())
                .orElseThrow(() -> new EmployeeNotFoundException(command.employeeId()));

        var business = businessRepository.findById(employee.getBusinessId())
                .orElseThrow(() -> new BusinessNotFoundException(employee.getBusinessId()));

        if (!business.isOwnedBy(command.requesterId())) {
            throw new UnauthorizedResourceException("You do not own the business this employee belongs to.");
        }

        employee.changeStatus(command.newStatus(), Instant.now(clock));
        return employeeRepository.save(employee);
    }
}