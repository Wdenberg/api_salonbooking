package com.company.salonbooking.business.application.usecase;

import com.company.salonbooking.business.domain.exception.BusinessNotFoundException;
import com.company.salonbooking.business.domain.model.Business;
import com.company.salonbooking.business.domain.repository.BusinessRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetBusinessUseCase {

    private final BusinessRepository businessRepository;

    public GetBusinessUseCase(BusinessRepository businessRepository) {
        this.businessRepository = businessRepository;
    }

    @Transactional(readOnly = true)
    public Business execute(UUID businessId) {
        return businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessNotFoundException(businessId));
    }
}