package com.company.salonbooking.catalog.infrastructure.persistence;

import com.company.salonbooking.catalog.domain.model.Money;
import com.company.salonbooking.catalog.domain.model.ServiceDuration;
import com.company.salonbooking.catalog.domain.model.ServiceOffering;
import com.company.salonbooking.catalog.domain.repository.ServiceOfferingRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ServiceOfferingRepositoryAdapter implements ServiceOfferingRepository {

    private final ServiceOfferingJpaRepository jpaRepository;

    public ServiceOfferingRepositoryAdapter(ServiceOfferingJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<ServiceOffering> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<ServiceOffering> findByBusinessId(UUID businessId, boolean onlyActive, int page, int size) {
        var pageable = PageRequest.of(page, size);
        var entities = onlyActive
                ? jpaRepository.findByBusinessIdAndActiveTrue(businessId, pageable)
                : jpaRepository.findByBusinessId(businessId, pageable);
        return entities.stream().map(this::toDomain).toList();
    }

    @Override
    public ServiceOffering save(ServiceOffering service) {
        ServiceOfferingJpaEntity entity = new ServiceOfferingJpaEntity(
                service.getId(), service.getBusinessId(), service.getName(), service.getDescription(),
                service.getPrice().getAmount(), service.getPrice().getCurrencyCode(),
                service.getDuration().toMinutes(), service.isActive(), service.getCreatedAt(), service.getUpdatedAt());
        return toDomain(jpaRepository.save(entity));
    }

    private ServiceOffering toDomain(ServiceOfferingJpaEntity entity) {
        return ServiceOffering.restore(entity.getId(), entity.getBusinessId(), entity.getName(), entity.getDescription(),
                Money.of(entity.getPriceAmount(), entity.getPriceCurrency()),
                ServiceDuration.ofMinutes(entity.getDurationMinutes()), entity.isActive(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }
}