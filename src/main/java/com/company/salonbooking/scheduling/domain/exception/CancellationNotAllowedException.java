package com.company.salonbooking.scheduling.domain.exception;

public class CancellationNotAllowedException extends RuntimeException {

    public CancellationNotAllowedException(String message) {
        super(message);
    }
}