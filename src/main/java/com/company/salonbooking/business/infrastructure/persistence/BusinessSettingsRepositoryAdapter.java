package com.company.salonbooking.business.infrastructure.persistence;

import com.company.salonbooking.business.domain.model.BusinessSettings;
import com.company.salonbooking.business.domain.repository.BusinessSettingsRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class BusinessSettingsRepositoryAdapter implements BusinessSettingsRepository {

    private final BusinessSettingsJpaRepository jpaRepository;

    public BusinessSettingsRepositoryAdapter(BusinessSettingsJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<BusinessSettings> findByBusinessId(UUID businessId) {
        return jpaRepository.findById(businessId).map(e -> BusinessSettings.restore(
                e.getBusinessId(), e.getMinimumAdvanceMinutes(), e.getMaximumAdvanceDays(),
                e.getCancellationMinimumMinutes(), e.getSlotIntervalMinutes(), e.getCreatedAt(), e.getUpdatedAt()));
    }

    @Override
    public BusinessSettings save(BusinessSettings settings) {
        BusinessSettingsJpaEntity entity = new BusinessSettingsJpaEntity(
                settings.getBusinessId(), settings.getMinimumAdvanceMinutes(), settings.getMaximumAdvanceDays(),
                settings.getCancellationMinimumMinutes(), settings.getSlotIntervalMinutes(),
                settings.getCreatedAt(), settings.getUpdatedAt());
        BusinessSettingsJpaEntity saved = jpaRepository.save(entity);
        return BusinessSettings.restore(saved.getBusinessId(), saved.getMinimumAdvanceMinutes(),
                saved.getMaximumAdvanceDays(), saved.getCancellationMinimumMinutes(), saved.getSlotIntervalMinutes(),
                saved.getCreatedAt(), saved.getUpdatedAt());
    }
}