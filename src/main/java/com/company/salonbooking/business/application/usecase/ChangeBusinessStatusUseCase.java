package com.company.salonbooking.business.application.usecase;

import com.company.salonbooking.business.application.command.ChangeBusinessStatusCommand;
import com.company.salonbooking.business.domain.exception.BusinessNotFoundException;
import com.company.salonbooking.business.domain.model.Business;
import com.company.salonbooking.business.domain.repository.BusinessRepository;
import com.company.salonbooking.shared.exception.UnauthorizedResourceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class ChangeBusinessStatusUseCase {

    private final BusinessRepository businessRepository;
    private final Clock clock;

    public ChangeBusinessStatusUseCase(BusinessRepository businessRepository, Clock clock) {
        this.businessRepository = businessRepository;
        this.clock = clock;
    }

    @Transactional
    public Business execute(ChangeBusinessStatusCommand command) {
        Business business = businessRepository.findById(command.businessId())
                .orElseThrow(() -> new BusinessNotFoundException(command.businessId()));

        if (!business.isOwnedBy(command.requesterId())) {
            throw new UnauthorizedResourceException("You do not own this business.");
        }

        business.changeStatus(command.newStatus(), Instant.now(clock));
        return businessRepository.save(business);
    }
}