package com.company.salonbooking.business.application.usecase;

import com.company.salonbooking.business.domain.model.OpeningHourInterval;
import com.company.salonbooking.business.domain.repository.BusinessOpeningHourRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GetOpeningHoursUseCase {

    private final BusinessOpeningHourRepository repository;

    public GetOpeningHoursUseCase(BusinessOpeningHourRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "business-opening-hours", key = "#businessId")
    public List<OpeningHourInterval> execute(UUID businessId) {
        return repository.findByBusinessId(businessId);
    }
}