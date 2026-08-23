package com.company.salonbooking.employee.infrastructure.persistence;

import com.company.salonbooking.employee.domain.model.AvailabilityBlock;
import com.company.salonbooking.employee.domain.repository.AvailabilityBlockRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AvailabilityBlockRepositoryAdapter implements AvailabilityBlockRepository {

    private final AvailabilityBlockJpaRepository jpaRepository;

    public AvailabilityBlockRepositoryAdapter(AvailabilityBlockJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<AvailabilityBlock> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<AvailabilityBlock> findByEmployeeIdAndRange(UUID employeeId, Instant from, Instant to) {
        return jpaRepository.findByEmployeeIdAndRange(employeeId, from, to).stream().map(this::toDomain).toList();
    }

    @Override
    public AvailabilityBlock save(AvailabilityBlock block) {
        AvailabilityBlockJpaEntity entity = new AvailabilityBlockJpaEntity(block.getId(), block.getEmployeeId(),
                block.getStartAt(), block.getEndAt(), block.getReason(), block.getCreatedAt());
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    private AvailabilityBlock toDomain(AvailabilityBlockJpaEntity entity) {
        return AvailabilityBlock.restore(entity.getId(), entity.getEmployeeId(), entity.getStartAt(),
                entity.getEndAt(), entity.getReason(), entity.getCreatedAt());
    }
}