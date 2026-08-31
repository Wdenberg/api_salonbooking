package com.company.salonbooking.scheduling.infrastructure.notification;

import com.company.salonbooking.business.domain.repository.BusinessRepository;
import com.company.salonbooking.notification.application.port.AppointmentContextResolver;
import com.company.salonbooking.scheduling.domain.exception.AppointmentNotFoundException;
import com.company.salonbooking.scheduling.domain.model.Appointment;
import com.company.salonbooking.scheduling.domain.repository.AppointmentRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AppointmentContextResolverAdapter implements AppointmentContextResolver {

    private final AppointmentRepository appointmentRepository;
    private final BusinessRepository businessRepository;

    public AppointmentContextResolverAdapter(AppointmentRepository appointmentRepository, BusinessRepository businessRepository) {
        this.appointmentRepository = appointmentRepository;
        this.businessRepository = businessRepository;
    }

    @Override
    public AppointmentContext resolve(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

        String businessName = businessRepository.findById(appointment.getBusinessId())
                .map(b -> b.getName())
                .orElse("");

        return new AppointmentContext(appointment.getId(), appointment.getCustomerId(), appointment.getEmployeeId(),
                appointment.getBusinessId(), businessName, appointment.getServiceNameSnapshot(),
                appointment.getEmployeeNameSnapshot(), appointment.getStartAt());
    }
}