package com.company.salonbooking.scheduling.application.usecase;

import com.company.salonbooking.business.domain.repository.BusinessRepository;
import com.company.salonbooking.employee.domain.repository.EmployeeRepository;
import com.company.salonbooking.scheduling.domain.exception.AppointmentNotFoundException;
import com.company.salonbooking.scheduling.domain.model.Appointment;
import com.company.salonbooking.scheduling.domain.repository.AppointmentRepository;
import com.company.salonbooking.shared.exception.UnauthorizedResourceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Enforces the IDOR protection from Seção 125: a customer may only read their own appointment. */
@Service
public class GetAppointmentUseCase {

    private final AppointmentRepository appointmentRepository;
    private final BusinessRepository businessRepository;
    private final EmployeeRepository employeeRepository;

    public GetAppointmentUseCase(AppointmentRepository appointmentRepository, BusinessRepository businessRepository,
                                 EmployeeRepository employeeRepository) {
        this.appointmentRepository = appointmentRepository;
        this.businessRepository = businessRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public Appointment execute(UUID appointmentId, UUID requesterId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

        boolean isCustomer = appointment.isOwnedByCustomer(requesterId);
        boolean isOwner = businessRepository.findById(appointment.getBusinessId())
                .map(b -> b.isOwnedBy(requesterId)).orElse(false);
        boolean isAssignedEmployee = employeeRepository.findById(appointment.getEmployeeId())
                .map(e -> e.isUser(requesterId)).orElse(false);

        if (!isCustomer && !isOwner && !isAssignedEmployee) {
            throw new UnauthorizedResourceException("You cannot view this appointment.");
        }

        return appointment;
    }
}