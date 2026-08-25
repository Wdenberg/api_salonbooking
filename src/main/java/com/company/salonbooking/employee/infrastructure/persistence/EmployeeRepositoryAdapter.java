package com.company.salonbooking.employee.infrastructure.persistence;

import com.company.salonbooking.employee.domain.model.Employee;
import com.company.salonbooking.employee.domain.repository.EmployeeRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class EmployeeRepositoryAdapter implements EmployeeRepository {

    private final EmployeeJpaRepository jpaRepository;

    public EmployeeRepositoryAdapter(EmployeeJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Employee> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Employee> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).map(this::toDomain);
    }

    @Override
    public List<Employee> findByBusinessId(UUID businessId, int page, int size) {
        return jpaRepository.findByBusinessId(businessId, PageRequest.of(page, size))
                .stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsByUserIdAndBusinessId(UUID userId, UUID businessId) {
        return jpaRepository.existsByUserIdAndBusinessId(userId, businessId);
    }

    @Override
    public Employee save(Employee employee) {
        EmployeeJpaEntity entity = new EmployeeJpaEntity(employee.getId(), employee.getUserId(), employee.getBusinessId(),
                employee.getSpecialty(), employee.getStatus(), employee.getCreatedAt(), employee.getUpdatedAt());
        return toDomain(jpaRepository.save(entity));
    }

    private Employee toDomain(EmployeeJpaEntity entity) {
        return Employee.restore(entity.getId(), entity.getUserId(), entity.getBusinessId(), entity.getSpecialty(),
                entity.getStatus(), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    @Override
    public List<Employee> findActiveByBusinessId(UUID businessId) {
        return jpaRepository.findByBusinessIdAndStatus(businessId, com.company.salonbooking.employee.domain.model.EmployeeStatus.ACTIVE)
                .stream().map(this::toDomain).toList();
    }
}