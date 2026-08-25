package com.company.salonbooking.catalog.application.usecase;

import com.company.salonbooking.catalog.domain.exception.ServiceOfferingNotFoundException;
import com.company.salonbooking.catalog.domain.model.ServiceOffering;
import com.company.salonbooking.catalog.domain.repository.ServiceOfferingRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetServiceUseCase {

    private final ServiceOfferingRepository serviceRepository;

    public GetServiceUseCase(ServiceOfferingRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "catalog-services", key = "#serviceId")
    public ServiceOffering execute(UUID serviceId) {
        return serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ServiceOfferingNotFoundException(serviceId));
    }
}