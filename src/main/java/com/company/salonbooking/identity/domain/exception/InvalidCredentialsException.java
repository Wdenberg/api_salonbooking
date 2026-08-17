package com.company.salonbooking.identity.domain.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(){
        super("Invalid email or password");
    }
}
