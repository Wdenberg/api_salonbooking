package com.company.salonbooking.catalog.application.usecase;

import com.company.salonbooking.catalog.domain.model.ServiceOffering;
import com.company.salonbooking.catalog.domain.repository.ServiceOfferingRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListServicesUseCase {

    private static final int MAX_PAGE_SIZE = 100;

    private final ServiceOfferingRepository serviceRepository;

    public ListServicesUseCase(ServiceOfferingRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    /**
     * Cached only for the common "active services, first page" read pattern used by
     * customers browsing a business. Other pages/filters bypass cache (Seção 37/38).
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "catalog-business-services", key = "#businessId",
            condition = "#onlyActive && #page == 0")
    public List<ServiceOffering> execute(UUID businessId, boolean onlyActive, int page, int size) {
        int safeSize = Math.min(size, MAX_PAGE_SIZE);
        return serviceRepository.findByBusinessId(businessId, onlyActive, page, safeSize);
    }
}