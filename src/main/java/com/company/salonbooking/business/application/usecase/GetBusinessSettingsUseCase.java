package com.company.salonbooking.business.application.usecase;

import com.company.salonbooking.business.domain.exception.BusinessNotFoundException;
import com.company.salonbooking.business.domain.model.BusinessSettings;
import com.company.salonbooking.business.domain.repository.BusinessSettingsRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetBusinessSettingsUseCase {

    private final BusinessSettingsRepository settingsRepository;

    public GetBusinessSettingsUseCase(BusinessSettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "business-settings", key = "#businessId")
    public BusinessSettings execute(UUID businessId) {
        return settingsRepository.findByBusinessId(businessId)
                .orElseThrow(() -> new BusinessNotFoundException(businessId));
    }
}