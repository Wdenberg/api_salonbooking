package com.company.salonbooking.customer.application.usecase;

import com.company.salonbooking.customer.domain.model.CustomerProfile;
import com.company.salonbooking.customer.domain.repository.CustomerProfileRepository;
import com.company.salonbooking.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetCustomerProfileUseCase {

    private final CustomerProfileRepository repository;

    public GetCustomerProfileUseCase(CustomerProfileRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public CustomerProfile execute(UUID userId) {
        return repository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found: " + userId));
    }
}