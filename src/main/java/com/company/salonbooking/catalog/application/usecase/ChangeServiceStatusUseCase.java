package com.company.salonbooking.catalog.application.usecase;

import com.company.salonbooking.business.domain.exception.BusinessNotFoundException;
import com.company.salonbooking.business.domain.repository.BusinessRepository;
import com.company.salonbooking.catalog.application.command.ChangeServiceStatusCommand;
import com.company.salonbooking.catalog.domain.exception.ServiceOfferingNotFoundException;
import com.company.salonbooking.catalog.domain.model.ServiceOffering;
import com.company.salonbooking.catalog.domain.repository.ServiceOfferingRepository;
import com.company.salonbooking.shared.exception.UnauthorizedResourceException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class ChangeServiceStatusUseCase {

    private final ServiceOfferingRepository serviceRepository;
    private final BusinessRepository businessRepository;
    private final Clock clock;

    public ChangeServiceStatusUseCase(ServiceOfferingRepository serviceRepository, BusinessRepository businessRepository, Clock clock) {
        this.serviceRepository = serviceRepository;
        this.businessRepository = businessRepository;
        this.clock = clock;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "catalog-services", key = "#command.serviceId()"),
            @CacheEvict(cacheNames = "catalog-business-services", allEntries = true)
    })
    public ServiceOffering execute(ChangeServiceStatusCommand command) {
        ServiceOffering service = serviceRepository.findById(command.serviceId())
                .orElseThrow(() -> new ServiceOfferingNotFoundException(command.serviceId()));

        var business = businessRepository.findById(service.getBusinessId())
                .orElseThrow(() -> new BusinessNotFoundException(service.getBusinessId()));

        if (!business.isOwnedBy(command.requesterId())) {
            throw new UnauthorizedResourceException("You do not own the business this service belongs to.");
        }

        Instant now = Instant.now(clock);
        if (command.active()) {
            service.activate(now);
        } else {
            service.deactivate(now);
        }

        return serviceRepository.save(service);
    }
}