package com.company.salonbooking.business.domain.repository;

import com.company.salonbooking.business.domain.model.BusinessSettings;

import java.util.Optional;
import java.util.UUID;

public interface BusinessSettingsRepository {

    Optional<BusinessSettings> findByBusinessId(UUID businessId);

    BusinessSettings save(BusinessSettings settings);
}