package com.company.salonbooking.business.domain.repository;

import com.company.salonbooking.business.domain.model.Business;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BusinessRepository {

    Optional<Business> findById(UUID id);

    Business save(Business business);
    Optional<Business> findByOwnerId(UUID ownerId);
    void deleteById(UUID id);

    /** Cross-tenant: finds all businesses across all owners (PLATFORM_ADMIN only). */
    List<Business> findAll(int page, int size);
}
