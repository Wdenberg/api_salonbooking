package com.company.salonbooking.scheduling.application.usecase;

import com.company.salonbooking.scheduling.domain.model.Appointment;
import com.company.salonbooking.scheduling.domain.model.AppointmentFilter;
import com.company.salonbooking.scheduling.domain.repository.AppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListAllAppointmentsUseCase {

    private static final int MAX_PAGE_SIZE = 100;

    private final AppointmentRepository appointmentRepository;

    public ListAllAppointmentsUseCase(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional(readOnly = true)
    public List<Appointment> execute(AppointmentFilter filter, int page, int size) {
        return appointmentRepository.findAll(filter, page, Math.min(size, MAX_PAGE_SIZE));
    }
}