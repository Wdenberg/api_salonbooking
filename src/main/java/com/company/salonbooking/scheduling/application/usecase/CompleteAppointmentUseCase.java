package com.company.salonbooking.scheduling.application.usecase;

import com.company.salonbooking.audit.domain.model.AuditAction;
import com.company.salonbooking.business.domain.repository.BusinessRepository;
import com.company.salonbooking.employee.domain.repository.EmployeeRepository;
import com.company.salonbooking.infrastructure.metrics.AppMetrics;
import com.company.salonbooking.scheduling.application.command.CompleteAppointmentCommand;
import com.company.salonbooking.scheduling.domain.event.AppointmentCompletedEvent;
import com.company.salonbooking.scheduling.domain.exception.AppointmentNotFoundException;
import com.company.salonbooking.scheduling.domain.model.Appointment;
import com.company.salonbooking.scheduling.domain.repository.AppointmentRepository;
import com.company.salonbooking.shared.application.port.AuditRecorder;
import com.company.salonbooking.shared.application.port.DomainEventPublisher;
import com.company.salonbooking.shared.exception.UnauthorizedResourceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class CompleteAppointmentUseCase {

    private final AppointmentRepository appointmentRepository;
    private final BusinessRepository businessRepository;
    private final EmployeeRepository employeeRepository;
    private final DomainEventPublisher domainEventPublisher;
    private final AuditRecorder auditRecorder;
    private final AppMetrics appMetrics;
    private final Clock clock;

    public CompleteAppointmentUseCase(AppointmentRepository appointmentRepository, BusinessRepository businessRepository,
                                      EmployeeRepository employeeRepository, DomainEventPublisher domainEventPublisher, AuditRecorder auditRecorder, AppMetrics appMetrics, Clock clock) {
        this.appointmentRepository = appointmentRepository;
        this.businessRepository = businessRepository;
        this.employeeRepository = employeeRepository;
        this.domainEventPublisher = domainEventPublisher;
        this.auditRecorder = auditRecorder;
        this.appMetrics = appMetrics;
        this.clock = clock;
    }

    @Transactional
    public Appointment execute(CompleteAppointmentCommand command) {
        Appointment appointment = appointmentRepository.findById(command.appointmentId())
                .orElseThrow(() -> new AppointmentNotFoundException(command.appointmentId()));

        authorizeStaff(appointment, command.requesterId());

        appointment.complete(Instant.now(clock));
        Appointment saved = appointmentRepository.save(appointment);
        appMetrics.incrementAppointmentCompleted();
        domainEventPublisher.publish(new AppointmentCompletedEvent(
                saved.getId(), saved.getBusinessId(), saved.getCustomerId(), saved.getEmployeeId(), saved.getStartAt()));

        auditRecorder.record(command.requesterId(), saved.getBusinessId(), AuditAction.COMPLETE_APPOINTMENT,
                "Appointment", saved.getId(), null);
        return saved;
    }

    private void authorizeStaff(Appointment appointment, UUID requesterId) {
        boolean isOwner = businessRepository.findById(appointment.getBusinessId())
                .map(b -> b.isOwnedBy(requesterId)).orElse(false);
        boolean isAssignedEmployee = employeeRepository.findById(appointment.getEmployeeId())
                .map(e -> e.isUser(requesterId)).orElse(false);

        if (!isOwner && !isAssignedEmployee) {
            throw new UnauthorizedResourceException("You cannot complete this appointment.");
        }
    }
}