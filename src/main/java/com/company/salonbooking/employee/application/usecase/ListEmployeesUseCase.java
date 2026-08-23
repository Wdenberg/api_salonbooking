package com.company.salonbooking.employee.application.usecase;

import com.company.salonbooking.employee.domain.model.Employee;
import com.company.salonbooking.employee.domain.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListEmployeesUseCase {

    private static final int MAX_PAGE_SIZE = 100;

    private final EmployeeRepository employeeRepository;

    public ListEmployeesUseCase(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public List<Employee> execute(UUID businessId, int page, int size) {
        int safeSize = Math.min(size, MAX_PAGE_SIZE);
        return employeeRepository.findByBusinessId(businessId, page, safeSize);
    }
}