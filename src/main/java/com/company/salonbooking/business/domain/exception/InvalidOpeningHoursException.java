package com.company.salonbooking.business.domain.exception;

public class InvalidOpeningHoursException extends RuntimeException {

    public InvalidOpeningHoursException(String message) {
        super(message);
    }
}
