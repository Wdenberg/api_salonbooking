package com.company.salonbooking.scheduling.domain.exception;

public class InvalidAppointmentTransitionException extends RuntimeException {

    public InvalidAppointmentTransitionException(String message) {
        super(message);
    }
}