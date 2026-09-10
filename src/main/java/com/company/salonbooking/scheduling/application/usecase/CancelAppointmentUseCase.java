package com.company.salonbooking.scheduling.application.usecase;

import com.company.salonbooking.audit.domain.model.AuditAction;
import com.company.salonbooking.business.domain.repository.BusinessRepository;
import com.company.salonbooking.business.domain.repository.BusinessSettingsRepository;
import com.company.salonbooking.employee.domain.repository.EmployeeRepository;
import com.company.salonbooking.infrastructure.metrics.AppMetrics;
import com.company.salonbooking.scheduling.application.command.CancelAppointmentCommand;
import com.company.salonbooking.scheduling.domain.event.AppointmentCancelledEvent;
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
public class CancelAppointmentUseCase {

    private final AppointmentRepository appointmentRepository;
    private final BusinessRepository businessRepository;
    private final BusinessSettingsRepository businessSettingsRepository;
    private final EmployeeRepository employeeRepository;
    private final DomainEventPublisher domainEventPublisher;
    private final AuditRecorder auditRecorder;
    private final AppMetrics appMetrics;
    private final Clock clock;

    public CancelAppointmentUseCase(AppointmentRepository appointmentRepository, BusinessRepository businessRepository,
                                    BusinessSettingsRepository businessSettingsRepository, EmployeeRepository employeeRepository,
                                    DomainEventPublisher domainEventPublisher, AuditRecorder auditRecorder, AppMetrics appMetrics, Clock clock) {
        this.appointmentRepository = appointmentRepository;
        this.businessRepository = businessRepository;
        this.businessSettingsRepository = businessSettingsRepository;
        this.employeeRepository = employeeRepository;
        this.domainEventPublisher = domainEventPublisher;
        this.auditRecorder = auditRecorder;
        this.appMetrics = appMetrics;
        this.clock = clock;
    }

    @Transactional
    public Appointment execute(CancelAppointmentCommand command) {
        Appointment appointment = appointmentRepository.findById(command.appointmentId())
                .orElseThrow(() -> new AppointmentNotFoundException(command.appointmentId()));

        authorize(appointment, command.requesterId());

        var settings = businessSettingsRepository.findByBusinessId(appointment.getBusinessId())
                .orElseThrow(() -> new AppointmentNotFoundException(command.appointmentId()));

        appointment.cancel(Instant.now(clock), settings.getCancellationMinimumMinutes());
        Appointment saved = appointmentRepository.save(appointment);
        appMetrics.incrementAppointmentCancelled();

        domainEventPublisher.publish(new AppointmentCancelledEvent(
                saved.getId(), saved.getBusinessId(), saved.getCustomerId(), saved.getEmployeeId(), saved.getStartAt()));

        auditRecorder.record(command.requesterId(), saved.getBusinessId(), AuditAction.CANCEL_APPOINTMENT,
                "Appointment", saved.getId(), null);
        return saved;
    }

    private void authorize(Appointment appointment, UUID requesterId) {
        boolean isCustomer = appointment.isOwnedByCustomer(requesterId);
        boolean isOwner = businessRepository.findById(appointment.getBusinessId())
                .map(b -> b.isOwnedBy(requesterId)).orElse(false);
        boolean isAssignedEmployee = employeeRepository.findById(appointment.getEmployeeId())
                .map(e -> e.isUser(requesterId)).orElse(false);

        if (!isCustomer && !isOwner && !isAssignedEmployee) {
            throw new UnauthorizedResourceException("You cannot cancel this appointment.");
        }
    }
}