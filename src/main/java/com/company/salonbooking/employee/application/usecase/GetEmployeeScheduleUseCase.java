package com.company.salonbooking.employee.application.usecase;

import com.company.salonbooking.employee.domain.model.EmployeeScheduleInterval;
import com.company.salonbooking.employee.domain.repository.EmployeeScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GetEmployeeScheduleUseCase {

    private final EmployeeScheduleRepository repository;

    public GetEmployeeScheduleUseCase(EmployeeScheduleRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<EmployeeScheduleInterval> execute(UUID employeeId) {
        return repository.findByEmployeeId(employeeId);
    }
}