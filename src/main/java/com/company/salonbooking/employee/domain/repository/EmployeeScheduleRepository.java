package com.company.salonbooking.employee.domain.repository;

import com.company.salonbooking.employee.domain.model.EmployeeScheduleInterval;

import java.util.List;
import java.util.UUID;

public interface EmployeeScheduleRepository {

    List<EmployeeScheduleInterval> findByEmployeeId(UUID employeeId);

    List<EmployeeScheduleInterval> replaceAll(UUID employeeId, List<EmployeeScheduleInterval> intervals);
}