package com.company.salonbooking.customer.infrastructure.persistence;

import com.company.salonbooking.customer.domain.model.CustomerProfile;
import com.company.salonbooking.customer.domain.repository.CustomerProfileRepository;
import com.company.salonbooking.identity.application.port.CustomerProfileInitializer;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Component
public class CustomerProfileInitializerAdapter implements CustomerProfileInitializer {

    private final CustomerProfileRepository repository;
    private final Clock clock;

    public CustomerProfileInitializerAdapter(CustomerProfileRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public void initializeFor(UUID userId) {
        repository.save(CustomerProfile.createEmpty(userId, Instant.now(clock)));
    }
}