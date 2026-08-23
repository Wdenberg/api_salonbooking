package com.company.salonbooking.customer.application.usecase;

import com.company.salonbooking.customer.application.command.UpdateCustomerProfileCommand;
import com.company.salonbooking.customer.domain.model.CustomerProfile;
import com.company.salonbooking.customer.domain.repository.CustomerProfileRepository;
import com.company.salonbooking.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class UpdateCustomerProfileUseCase {

    private final CustomerProfileRepository repository;
    private final Clock clock;

    public UpdateCustomerProfileUseCase(CustomerProfileRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public CustomerProfile execute(UpdateCustomerProfileCommand command) {
        CustomerProfile profile = repository.findByUserId(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found: " + command.userId()));

        profile.update(command.phone(), command.dateOfBirth(), Instant.now(clock));
        return repository.save(profile);
    }
}