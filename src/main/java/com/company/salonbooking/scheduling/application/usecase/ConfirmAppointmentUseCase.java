package com.company.salonbooking.scheduling.application.usecase;

import com.company.salonbooking.business.domain.repository.BusinessRepository;
import com.company.salonbooking.employee.domain.repository.EmployeeRepository;
import com.company.salonbooking.scheduling.application.command.ConfirmAppointmentCommand;
import com.company.salonbooking.scheduling.domain.event.AppointmentConfirmedEvent;
import com.company.salonbooking.scheduling.domain.exception.AppointmentNotFoundException;
import com.company.salonbooking.scheduling.domain.model.Appointment;
import com.company.salonbooking.scheduling.domain.repository.AppointmentRepository;
import com.company.salonbooking.shared.application.port.DomainEventPublisher;
import com.company.salonbooking.shared.exception.UnauthorizedResourceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class ConfirmAppointmentUseCase {

    private final AppointmentRepository appointmentRepository;
    private final BusinessRepository businessRepository;
    private final EmployeeRepository employeeRepository;
    private final DomainEventPublisher domainEventPublisher;
    private final Clock clock;

    public ConfirmAppointmentUseCase(AppointmentRepository appointmentRepository, BusinessRepository businessRepository,
                                     EmployeeRepository employeeRepository, DomainEventPublisher domainEventPublisher, Clock clock) {
        this.appointmentRepository = appointmentRepository;
        this.businessRepository = businessRepository;
        this.employeeRepository = employeeRepository;
        this.domainEventPublisher = domainEventPublisher;
        this.clock = clock;
    }

    @Transactional
    public Appointment execute(ConfirmAppointmentCommand command) {
        Appointment appointment = appointmentRepository.findById(command.appointmentId())
                .orElseThrow(() -> new AppointmentNotFoundException(command.appointmentId()));

        authorizeStaff(appointment, command.requesterId());

        appointment.confirm(Instant.now(clock));
        Appointment saved = appointmentRepository.save(appointment);

        domainEventPublisher.publish(new AppointmentConfirmedEvent(
                saved.getId(), saved.getBusinessId(), saved.getCustomerId(), saved.getEmployeeId(), saved.getStartAt()));

        return saved;
    }

    private void authorizeStaff(Appointment appointment, UUID requesterId) {
        boolean isOwner = businessRepository.findById(appointment.getBusinessId())
                .map(b -> b.isOwnedBy(requesterId)).orElse(false);
        boolean isAssignedEmployee = employeeRepository.findById(appointment.getEmployeeId())
                .map(e -> e.isUser(requesterId)).orElse(false);

        if (!isOwner && !isAssignedEmployee) {
            throw new UnauthorizedResourceException("You cannot confirm this appointment.");
        }
    }
}