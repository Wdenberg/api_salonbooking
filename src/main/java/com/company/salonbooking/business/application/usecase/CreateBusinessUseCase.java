package com.company.salonbooking.business.application.usecase;

import com.company.salonbooking.business.application.command.CreateBusinessCommand;
import com.company.salonbooking.business.domain.model.Business;
import com.company.salonbooking.business.domain.model.BusinessSettings;
import com.company.salonbooking.business.domain.repository.BusinessRepository;
import com.company.salonbooking.business.domain.repository.BusinessSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

@Service
public class CreateBusinessUseCase {

    private final BusinessRepository businessRepository;
    private final BusinessSettingsRepository settingsRepository;
    private final Clock clock;

    public CreateBusinessUseCase(BusinessRepository businessRepository, BusinessSettingsRepository settingsRepository, Clock clock) {
        this.businessRepository = businessRepository;
        this.settingsRepository = settingsRepository;
        this.clock = clock;
    }

    @Transactional
    public Business execute(CreateBusinessCommand command) {
        Instant now = Instant.now(clock);
        ZoneId zoneId = ZoneId.of(command.timezone());

        Business business = Business.create(UUID.randomUUID(), command.ownerId(), command.name(),
                command.description(), command.phone(), command.email(), command.address(), zoneId, now);

        Business saved = businessRepository.save(business);

        // Default settings created automatically so the business is immediately schedulable (Seção 133).
        settingsRepository.save(BusinessSettings.defaultsFor(saved.getId(), now));

        return saved;
    }
}