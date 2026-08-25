package com.company.salonbooking.catalog.domain.repository;

import com.company.salonbooking.catalog.domain.model.ServiceOffering;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceOfferingRepository {

    Optional<ServiceOffering> findById(UUID id);

    List<ServiceOffering> findByBusinessId(UUID businessId, boolean onlyActive, int page, int size);

    ServiceOffering save(ServiceOffering service);
}