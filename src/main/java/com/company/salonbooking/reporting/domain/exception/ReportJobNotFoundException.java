package com.company.salonbooking.reporting.domain.exception;

import com.company.salonbooking.shared.exception.ResourceNotFoundException;

import java.util.UUID;

public class ReportJobNotFoundException extends ResourceNotFoundException {

    public ReportJobNotFoundException(UUID id) {
        super("Report job not found: " + id);
    }
}