package com.company.salonbooking.business.infrastructure.persistence;

import com.company.salonbooking.business.domain.model.Business;
import com.company.salonbooking.business.domain.repository.BusinessRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class BusinessRepositoryAdapter implements BusinessRepository {

    private final BusinessJpaRepository jpaRepository;

    public BusinessRepositoryAdapter(BusinessJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Business> findById(UUID id) {
        return jpaRepository.findById(id).map(BusinessMapper::toDomain);
    }

    @Override
    public Business save(Business business) {
        return BusinessMapper.toDomain(jpaRepository.save(BusinessMapper.toEntity(business)));
    }
}