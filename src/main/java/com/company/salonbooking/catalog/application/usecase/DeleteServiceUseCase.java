package com.company.salonbooking.catalog.application.usecase;

import com.company.salonbooking.audit.domain.model.AuditAction;
import com.company.salonbooking.scheduling.domain.repository.AppointmentRepository;
import com.company.salonbooking.catalog.application.command.DeleteServiceCommand;
import com.company.salonbooking.catalog.domain.exception.ServiceOfferingNotFoundException;
import com.company.salonbooking.catalog.domain.model.ServiceOffering;
import com.company.salonbooking.catalog.domain.repository.ServiceOfferingRepository;
import com.company.salonbooking.shared.application.port.AuditRecorder;
import com.company.salonbooking.shared.exception.UnauthorizedResourceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class DeleteServiceUseCase {

    private final ServiceOfferingRepository serviceRepository;
    private final AppointmentRepository appointmentRepository;
    private final AuditRecorder auditRecorder;
    private final Clock clock;

    public DeleteServiceUseCase(ServiceOfferingRepository serviceRepository, AppointmentRepository appointmentRepository,
                                AuditRecorder auditRecorder, Clock clock) {
        this.serviceRepository = serviceRepository;
        this.appointmentRepository = appointmentRepository;
        this.auditRecorder = auditRecorder;
        this.clock = clock;
    }

    @Transactional
    public void execute(DeleteServiceCommand command) {
        ServiceOffering service = serviceRepository.findById(command.serviceId())
                .orElseThrow(() -> new ServiceOfferingNotFoundException(command.serviceId()));

        if (!service.isOwnedBy(command.requesterId())) {
            throw new UnauthorizedResourceException("You do not own this service.");
        }

        // Check for active appointments
        long activeAppointments = appointmentRepository.countActiveByServiceId(command.serviceId());
        if (activeAppointments > 0) {
            throw new IllegalStateException("Cannot delete service with active appointments. Cancel or complete them first.");
        }

        // Soft delete: set active to false
        service.deactivate(Instant.now(clock));
        serviceRepository.save(service);

        auditRecorder.record(command.requesterId(), service.getBusinessId(), AuditAction.DELETE_SERVICE, "Service", command.serviceId(), null);
    }
}