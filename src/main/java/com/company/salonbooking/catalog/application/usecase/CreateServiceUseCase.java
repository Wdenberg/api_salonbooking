package com.company.salonbooking.catalog.application.usecase;

import com.company.salonbooking.business.domain.exception.BusinessNotFoundException;
import com.company.salonbooking.business.domain.model.Business;
import com.company.salonbooking.business.domain.repository.BusinessRepository;
import com.company.salonbooking.catalog.application.command.CreateServiceCommand;
import com.company.salonbooking.catalog.domain.model.Money;
import com.company.salonbooking.catalog.domain.model.ServiceDuration;
import com.company.salonbooking.catalog.domain.model.ServiceOffering;
import com.company.salonbooking.catalog.domain.repository.ServiceOfferingRepository;
import com.company.salonbooking.shared.exception.UnauthorizedResourceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class CreateServiceUseCase {

    private final BusinessRepository businessRepository;
    private final ServiceOfferingRepository serviceRepository;
    private final Clock clock;

    public CreateServiceUseCase(BusinessRepository businessRepository, ServiceOfferingRepository serviceRepository, Clock clock) {
        this.businessRepository = businessRepository;
        this.serviceRepository = serviceRepository;
        this.clock = clock;
    }

    @Transactional
    public ServiceOffering execute(CreateServiceCommand command) {
        Business business = businessRepository.findById(command.businessId())
                .orElseThrow(() -> new BusinessNotFoundException(command.businessId()));

        if (!business.isOwnedBy(command.requesterId())) {
            throw new UnauthorizedResourceException("You do not own this business.");
        }

        Money price = Money.of(command.priceAmount(), command.priceCurrency());
        ServiceDuration duration = ServiceDuration.ofMinutes(command.durationMinutes());

        ServiceOffering service = ServiceOffering.create(UUID.randomUUID(), business.getId(), command.name(),
                command.description(), price, duration, Instant.now(clock));

        return serviceRepository.save(service);
    }
}