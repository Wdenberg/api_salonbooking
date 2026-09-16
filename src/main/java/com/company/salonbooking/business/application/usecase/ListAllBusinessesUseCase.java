package com.company.salonbooking.business.application.usecase;

import com.company.salonbooking.business.domain.model.Business;
import com.company.salonbooking.business.domain.repository.BusinessRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListAllBusinessesUseCase {

    private static final int MAX_PAGE_SIZE = 100;

    private final BusinessRepository businessRepository;

    public ListAllBusinessesUseCase(BusinessRepository businessRepository) {
        this.businessRepository = businessRepository;
    }

    @Transactional(readOnly = true)
    public List<Business> execute(int page, int size) {
        return businessRepository.findAll(page, Math.min(size, MAX_PAGE_SIZE));
    }
}