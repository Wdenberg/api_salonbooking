package com.company.salonbooking.customer.infrastructure.persistence;

import com.company.salonbooking.customer.domain.model.CustomerProfile;
import com.company.salonbooking.customer.domain.repository.CustomerProfileRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class CustomerProfileRepositoryAdapter implements CustomerProfileRepository {

    private final CustomerProfileJpaRepository jpaRepository;

    public CustomerProfileRepositoryAdapter(CustomerProfileJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<CustomerProfile> findByUserId(UUID userId) {
        return jpaRepository.findById(userId).map(e ->
                CustomerProfile.restore(e.getUserId(), e.getPhone(), e.getDateOfBirth(), e.getCreatedAt(), e.getUpdatedAt()));
    }

    @Override
    public CustomerProfile save(CustomerProfile profile) {
        CustomerProfileJpaEntity entity = new CustomerProfileJpaEntity(profile.getUserId(), profile.getPhone(),
                profile.getDateOfBirth(), profile.getCreatedAt(), profile.getUpdatedAt());
        CustomerProfileJpaEntity saved = jpaRepository.save(entity);
        return CustomerProfile.restore(saved.getUserId(), saved.getPhone(), saved.getDateOfBirth(),
                saved.getCreatedAt(), saved.getUpdatedAt());
    }
}