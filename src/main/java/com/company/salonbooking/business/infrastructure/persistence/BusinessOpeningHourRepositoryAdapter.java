package com.company.salonbooking.business.infrastructure.persistence;

import com.company.salonbooking.business.domain.model.OpeningHourInterval;
import com.company.salonbooking.business.domain.model.TimeRange;
import com.company.salonbooking.business.domain.repository.BusinessOpeningHourRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public class BusinessOpeningHourRepositoryAdapter implements BusinessOpeningHourRepository {

    private final BusinessOpeningHourJpaRepository jpaRepository;

    public BusinessOpeningHourRepositoryAdapter(BusinessOpeningHourJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<OpeningHourInterval> findByBusinessId(UUID businessId) {
        return jpaRepository.findByBusinessId(businessId).stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional
    public List<OpeningHourInterval> replaceAll(UUID businessId, List<OpeningHourInterval> intervals) {
        jpaRepository.deleteByBusinessId(businessId);
        jpaRepository.flush();

        List<BusinessOpeningHourJpaEntity> entities = intervals.stream()
                .map(interval -> new BusinessOpeningHourJpaEntity(
                        interval.getId(), businessId, interval.getDayOfWeek(),
                        interval.getTimeRange().start(), interval.getTimeRange().end()))
                .toList();

        return jpaRepository.saveAll(entities).stream().map(this::toDomain).toList();
    }

    private OpeningHourInterval toDomain(BusinessOpeningHourJpaEntity entity) {
        return new OpeningHourInterval(entity.getId(), entity.getDayOfWeek(),
                new TimeRange(entity.getOpenTime(), entity.getCloseTime()));
    }
}