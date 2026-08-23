package com.company.salonbooking.employee.domain.repository;

import com.company.salonbooking.employee.domain.model.Employee;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository {

    Optional<Employee> findById(UUID id);

    Optional<Employee> findByUserId(UUID userId);

    List<Employee> findByBusinessId(UUID businessId, int page, int size);

    boolean existsByUserIdAndBusinessId(UUID userId, UUID businessId);

    Employee save(Employee employee);
}