package com.company.salonbooking.catalog.application.usecase;

import com.company.salonbooking.business.domain.exception.BusinessNotFoundException;
import com.company.salonbooking.business.domain.repository.BusinessRepository;
import com.company.salonbooking.catalog.application.command.UpdateServiceCommand;
import com.company.salonbooking.catalog.domain.exception.ServiceOfferingNotFoundException;
import com.company.salonbooking.shared.domain.model.Money;
import com.company.salonbooking.catalog.domain.model.ServiceDuration;
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
public class UpdateServiceUseCase {

    private final ServiceOfferingRepository serviceRepository;
    private final BusinessRepository businessRepository;
    private final Clock clock;

    public UpdateServiceUseCase(ServiceOfferingRepository serviceRepository, BusinessRepository businessRepository, Clock clock) {
        this.serviceRepository = serviceRepository;
        this.businessRepository = businessRepository;
        this.clock = clock;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "catalog-services", key = "#command.serviceId()"),
            @CacheEvict(cacheNames = "catalog-business-services", allEntries = true)
    })
    public ServiceOffering execute(UpdateServiceCommand command) {
        ServiceOffering service = serviceRepository.findById(command.serviceId())
                .orElseThrow(() -> new ServiceOfferingNotFoundException(command.serviceId()));

        var business = businessRepository.findById(service.getBusinessId())
                .orElseThrow(() -> new BusinessNotFoundException(service.getBusinessId()));

        if (!business.isOwnedBy(command.requesterId())) {
            throw new UnauthorizedResourceException("You do not own the business this service belongs to.");
        }

        Money price = Money.of(command.priceAmount(), command.priceCurrency());
        ServiceDuration duration = ServiceDuration.ofMinutes(command.durationMinutes());

        service.update(command.name(), command.description(), price, duration, Instant.now(clock));
        return serviceRepository.save(service);
    }
}