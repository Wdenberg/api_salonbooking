package com.company.salonbooking.scheduling.application.usecase;

import com.company.salonbooking.scheduling.domain.model.Appointment;
import com.company.salonbooking.scheduling.domain.repository.AppointmentFilter;
import com.company.salonbooking.scheduling.domain.repository.AppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListCustomerAppointmentsUseCase {

    private static final int MAX_PAGE_SIZE = 100;

    private final AppointmentRepository appointmentRepository;

    public ListCustomerAppointmentsUseCase(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional(readOnly = true)
    public List<Appointment> execute(UUID customerId, AppointmentFilter filter, int page, int size) {
        return appointmentRepository.findByCustomerId(customerId, filter, page, Math.min(size, MAX_PAGE_SIZE));
    }
}