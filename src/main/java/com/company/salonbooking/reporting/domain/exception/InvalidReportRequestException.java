package com.company.salonbooking.reporting.domain.exception;

public class InvalidReportRequestException extends RuntimeException {

    public InvalidReportRequestException(String message) {
        super(message);
    }
}