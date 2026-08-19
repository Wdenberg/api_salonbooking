package com.company.salonbooking.business.domain.exception;

import com.company.salonbooking.shared.exception.ResourceNotFoundException;

import java.util.UUID;

public class BusinessNotFoundException extends ResourceNotFoundException {

    public BusinessNotFoundException(UUID id) {
        super("Business not found: " + id);
    }
}