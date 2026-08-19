package com.company.salonbooking.business.application.usecase;

import com.company.salonbooking.business.application.command.UpdateBusinessCommand;
import com.company.salonbooking.business.domain.exception.BusinessNotFoundException;
import com.company.salonbooking.business.domain.model.Business;
import com.company.salonbooking.business.domain.repository.BusinessRepository;
import com.company.salonbooking.shared.exception.UnauthorizedResourceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class UpdateBusinessUseCase {

    private final BusinessRepository businessRepository;
    private final Clock clock;

    public UpdateBusinessUseCase(BusinessRepository businessRepository, Clock clock) {
        this.businessRepository = businessRepository;
        this.clock = clock;
    }

    @Transactional
    public Business execute(UpdateBusinessCommand command) {
        Business business = businessRepository.findById(command.businessId())
                .orElseThrow(() -> new BusinessNotFoundException(command.businessId()));

        // Ownership boundary (Seção 51): only the owner of THIS business may modify it.
        if (!business.isOwnedBy(command.requesterId())) {
            throw new UnauthorizedResourceException("You do not own this business.");
        }

        business.update(command.name(), command.description(), command.phone(), command.email(),
                command.address(), Instant.now(clock));

        return businessRepository.save(business);
    }
}