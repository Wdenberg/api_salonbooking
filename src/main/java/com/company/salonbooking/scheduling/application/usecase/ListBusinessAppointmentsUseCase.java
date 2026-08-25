package com.company.salonbooking.scheduling.application.usecase;

import com.company.salonbooking.business.domain.exception.BusinessNotFoundException;
import com.company.salonbooking.business.domain.repository.BusinessRepository;
import com.company.salonbooking.scheduling.domain.model.Appointment;
import com.company.salonbooking.scheduling.domain.repository.AppointmentFilter;
import com.company.salonbooking.scheduling.domain.repository.AppointmentRepository;
import com.company.salonbooking.shared.exception.UnauthorizedResourceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListBusinessAppointmentsUseCase {

    private static final int MAX_PAGE_SIZE = 100;

    private final AppointmentRepository appointmentRepository;
    private final BusinessRepository businessRepository;

    public ListBusinessAppointmentsUseCase(AppointmentRepository appointmentRepository, BusinessRepository businessRepository) {
        this.appointmentRepository = appointmentRepository;
        this.businessRepository = businessRepository;
    }

    @Transactional(readOnly = true)
    public List<Appointment> execute(UUID businessId, UUID requesterId, AppointmentFilter filter, int page, int size) {
        var business = businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessNotFoundException(businessId));

        if (!business.isOwnedBy(requesterId)) {
            throw new UnauthorizedResourceException("You do not own this business.");
        }

        return appointmentRepository.findByBusinessId(businessId, filter, page, Math.min(size, MAX_PAGE_SIZE));
    }
}