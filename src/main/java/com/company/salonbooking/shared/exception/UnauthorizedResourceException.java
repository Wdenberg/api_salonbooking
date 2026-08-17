package com.company.salonbooking.shared.exception;

/** Thrown when an authenticated user tries to access a resource outside their ownership/tenant boundary. */
public class UnauthorizedResourceException extends RuntimeException {

    public UnauthorizedResourceException(String message) {
        super(message);
    }
}