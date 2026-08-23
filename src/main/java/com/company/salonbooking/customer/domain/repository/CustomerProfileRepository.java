package com.company.salonbooking.customer.domain.repository;

import com.company.salonbooking.customer.domain.model.CustomerProfile;

import java.util.Optional;
import java.util.UUID;

public interface CustomerProfileRepository {

    Optional<CustomerProfile> findByUserId(UUID userId);

    CustomerProfile save(CustomerProfile profile);
}