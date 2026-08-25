package com.company.salonbooking.catalog.domain.exception;

import com.company.salonbooking.shared.exception.ResourceNotFoundException;

import java.util.UUID;

public class ServiceOfferingNotFoundException extends ResourceNotFoundException {

    public ServiceOfferingNotFoundException(UUID id) {
        super("Service not found: " + id);
    }
}