package com.company.salonbooking.scheduling.domain.exception;

import com.company.salonbooking.shared.exception.ResourceNotFoundException;

import java.util.UUID;

public class AppointmentNotFoundException extends ResourceNotFoundException {

    public AppointmentNotFoundException(UUID id) {
        super("Appointment not found: " + id);
    }
}