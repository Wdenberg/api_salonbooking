package com.company.salonbooking.business.domain.repository;

import com.company.salonbooking.business.domain.model.Business;

import java.util.Optional;
import java.util.UUID;

public interface BusinessRepository {

    Optional<Business> findById(UUID id);

    Business save(Business business);
}