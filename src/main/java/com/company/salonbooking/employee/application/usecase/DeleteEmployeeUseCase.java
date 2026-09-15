package com.company.salonbooking.employee.application.usecase;

import com.company.salonbooking.audit.domain.model.AuditAction;
import com.company.salonbooking.scheduling.domain.repository.AppointmentRepository;
import com.company.salonbooking.employee.application.command.DeleteEmployeeCommand;
import com.company.salonbooking.employee.domain.exception.EmployeeNotFoundException;
import com.company.salonbooking.employee.domain.model.Employee;
import com.company.salonbooking.employee.domain.repository.EmployeeRepository;
import com.company.salonbooking.shared.application.port.AuditRecorder;
import com.company.salonbooking.shared.exception.UnauthorizedResourceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class DeleteEmployeeUseCase {

    private final EmployeeRepository employeeRepository;
    private final AppointmentRepository appointmentRepository;
    private final AuditRecorder auditRecorder;
    private final Clock clock;

    public DeleteEmployeeUseCase(EmployeeRepository employeeRepository, AppointmentRepository appointmentRepository,
                                 AuditRecorder auditRecorder, Clock clock) {
        this.employeeRepository = employeeRepository;
        this.appointmentRepository = appointmentRepository;
        this.auditRecorder = auditRecorder;
        this.clock = clock;
    }

    @Transactional
    public void execute(DeleteEmployeeCommand command) {
        Employee employee = employeeRepository.findById(command.employeeId())
                .orElseThrow(() -> new EmployeeNotFoundException(command.employeeId()));

        if (!employee.isOwnedBy(command.requesterId())) {
            throw new UnauthorizedResourceException("You do not own this employee.");
        }

        // Check for active appointments
        long activeAppointments = appointmentRepository.countActiveByEmployeeId(command.employeeId());
        if (activeAppointments > 0) {
            throw new IllegalStateException("Cannot delete employee with active appointments. Cancel or complete them first.");
        }

        // Soft delete: change status to INACTIVE
        employee.changeStatus(com.company.salonbooking.employee.domain.model.EmployeeStatus.INACTIVE, Instant.now(clock));
        employeeRepository.save(employee);

        auditRecorder.record(command.requesterId(), employee.getBusinessId(), AuditAction.DELETE_EMPLOYEE, "Employee", command.employeeId(), null);
    }
}