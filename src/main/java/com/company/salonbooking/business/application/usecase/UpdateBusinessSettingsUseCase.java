package com.company.salonbooking.business.application.usecase;

import com.company.salonbooking.business.application.command.UpdateBusinessSettingsCommand;
import com.company.salonbooking.business.domain.exception.BusinessNotFoundException;
import com.company.salonbooking.business.domain.model.Business;
import com.company.salonbooking.business.domain.model.BusinessSettings;
import com.company.salonbooking.business.domain.repository.BusinessRepository;
import com.company.salonbooking.business.domain.repository.BusinessSettingsRepository;
import com.company.salonbooking.shared.exception.UnauthorizedResourceException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class UpdateBusinessSettingsUseCase {

    private final BusinessRepository businessRepository;
    private final BusinessSettingsRepository settingsRepository;
    private final Clock clock;

    public UpdateBusinessSettingsUseCase(BusinessRepository businessRepository,
                                         BusinessSettingsRepository settingsRepository, Clock clock) {
        this.businessRepository = businessRepository;
        this.settingsRepository = settingsRepository;
        this.clock = clock;
    }

    @Transactional
    @CacheEvict(cacheNames = "business-settings", key = "#command.businessId()")
    public BusinessSettings execute(UpdateBusinessSettingsCommand command) {
        Business business = businessRepository.findById(command.businessId())
                .orElseThrow(() -> new BusinessNotFoundException(command.businessId()));

        if (!business.isOwnedBy(command.requesterId())) {
            throw new UnauthorizedResourceException("You do not own this business.");
        }

        BusinessSettings settings = settingsRepository.findByBusinessId(command.businessId())
                .orElseThrow(() -> new BusinessNotFoundException(command.businessId()));

        settings.update(command.minimumAdvanceMinutes(), command.maximumAdvanceDays(),
                command.cancellationMinimumMinutes(), command.slotIntervalMinutes(), Instant.now(clock));

        return settingsRepository.save(settings);
    }
}