package com.company.salonbooking.employee.application.usecase;

import com.company.salonbooking.audit.domain.model.AuditAction;
import com.company.salonbooking.business.domain.exception.BusinessNotFoundException;
import com.company.salonbooking.business.domain.repository.BusinessRepository;
import com.company.salonbooking.employee.application.command.ChangeEmployeeStatusCommand;
import com.company.salonbooking.employee.domain.exception.EmployeeNotFoundException;
import com.company.salonbooking.employee.domain.model.Employee;
import com.company.salonbooking.employee.domain.model.EmployeeStatus;
import com.company.salonbooking.employee.domain.repository.EmployeeRepository;
import com.company.salonbooking.shared.application.port.AuditRecorder;
import com.company.salonbooking.shared.exception.UnauthorizedResourceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class ChangeEmployeeStatusUseCase {

    private final EmployeeRepository employeeRepository;
    private final BusinessRepository businessRepository;
    private final AuditRecorder auditRecorder;
    private final Clock clock;

    public ChangeEmployeeStatusUseCase(EmployeeRepository employeeRepository, BusinessRepository businessRepository,  AuditRecorder auditRecorder, Clock clock) {
        this.employeeRepository = employeeRepository;
        this.businessRepository = businessRepository;
        this.auditRecorder = auditRecorder;
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
        Employee saved = employeeRepository.save(employee);
        if (command.newStatus() == EmployeeStatus.INACTIVE) {
            auditRecorder.record(command.requesterId(), business.getId(), AuditAction.DELETE_EMPLOYEE, "Employee",
                    saved.getId(), null);
        }

        return saved;
    }
}