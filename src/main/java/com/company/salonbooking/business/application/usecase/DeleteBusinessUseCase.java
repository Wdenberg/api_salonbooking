package com.company.salonbooking.business.application.usecase;

import com.company.salonbooking.audit.domain.model.AuditAction;
import com.company.salonbooking.scheduling.domain.repository.AppointmentRepository;
import com.company.salonbooking.business.application.command.DeleteBusinessCommand;
import com.company.salonbooking.business.domain.exception.BusinessNotFoundException;
import com.company.salonbooking.business.domain.model.Business;
import com.company.salonbooking.business.domain.model.BusinessStatus;
import com.company.salonbooking.business.domain.repository.BusinessRepository;
import com.company.salonbooking.shared.application.port.AuditRecorder;
import com.company.salonbooking.shared.exception.UnauthorizedResourceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class DeleteBusinessUseCase {

    private final BusinessRepository businessRepository;
    private final AppointmentRepository appointmentRepository;
    private final AuditRecorder auditRecorder;
    private final Clock clock;

    public DeleteBusinessUseCase(BusinessRepository businessRepository, AppointmentRepository appointmentRepository,
                                 AuditRecorder auditRecorder, Clock clock) {
        this.businessRepository = businessRepository;
        this.appointmentRepository = appointmentRepository;
        this.auditRecorder = auditRecorder;
        this.clock = clock;
    }

    @Transactional
    public void execute(DeleteBusinessCommand command) {
        Business business = businessRepository.findById(command.businessId())
                .orElseThrow(() -> new BusinessNotFoundException(command.businessId()));

        if (!business.isOwnedBy(command.requesterId())) {
            throw new UnauthorizedResourceException("You do not own this business.");
        }

        // Check for active appointments - prevent deletion if there are PENDING or CONFIRMED appointments
        long activeAppointments = appointmentRepository.countActiveByBusinessId(command.businessId());
        if (activeAppointments > 0) {
            throw new IllegalStateException("Cannot delete business with active appointments. Cancel or complete them first.");
        }

        // Soft delete: change status to SUSPENDED
        business.changeStatus(BusinessStatus.SUSPENDED, Instant.now(clock));
        businessRepository.save(business);

        auditRecorder.record(command.requesterId(), command.businessId(), AuditAction.DELETE_BUSINESS, "Business", command.businessId(), null);
    }
}