package com.company.salonbooking.employee.infrastructure.persistence;

import com.company.salonbooking.employee.domain.model.EmployeeScheduleInterval;
import com.company.salonbooking.employee.domain.repository.EmployeeScheduleRepository;
import com.company.salonbooking.shared.domain.model.TimeRange;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public class EmployeeScheduleRepositoryAdapter implements EmployeeScheduleRepository {

    private final EmployeeScheduleJpaRepository jpaRepository;

    public EmployeeScheduleRepositoryAdapter(EmployeeScheduleJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<EmployeeScheduleInterval> findByEmployeeId(UUID employeeId) {
        return jpaRepository.findByEmployeeId(employeeId).stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional
    public List<EmployeeScheduleInterval> replaceAll(UUID employeeId, List<EmployeeScheduleInterval> intervals) {
        jpaRepository.deleteByEmployeeId(employeeId);
        jpaRepository.flush();

        List<EmployeeScheduleJpaEntity> entities = intervals.stream()
                .map(i -> new EmployeeScheduleJpaEntity(i.getId(), employeeId, i.getDayOfWeek(),
                        i.getTimeRange().start(), i.getTimeRange().end()))
                .toList();

        return jpaRepository.saveAll(entities).stream().map(this::toDomain).toList();
    }

    private EmployeeScheduleInterval toDomain(EmployeeScheduleJpaEntity entity) {
        return new EmployeeScheduleInterval(entity.getId(), entity.getDayOfWeek(),
                new TimeRange(entity.getStartTime(), entity.getEndTime()));
    }
}