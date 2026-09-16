package com.company.salonbooking.business.infrastructure.persistence;

import com.company.salonbooking.business.domain.model.Business;
import com.company.salonbooking.business.domain.repository.BusinessRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Override
    public Optional<Business> findByOwnerId(UUID ownerId) {
        return jpaRepository.findByOwnerId(ownerId).map(BusinessMapper::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<Business> findAll(int page, int size) {
        return jpaRepository.findAll(PageRequest.of(page, size)).getContent().stream().map(BusinessMapper::toDomain).toList();
    }
}
