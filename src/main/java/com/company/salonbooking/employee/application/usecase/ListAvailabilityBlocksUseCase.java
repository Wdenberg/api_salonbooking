package com.company.salonbooking.employee.application.usecase;

import com.company.salonbooking.employee.domain.model.AvailabilityBlock;
import com.company.salonbooking.employee.domain.repository.AvailabilityBlockRepository;
import com.company.salonbooking.employee.domain.repository.EmployeeRepository;
import com.company.salonbooking.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListAvailabilityBlocksUseCase {

    private final AvailabilityBlockRepository availabilityBlockRepository;
    private final EmployeeRepository employeeRepository;

    public ListAvailabilityBlocksUseCase(AvailabilityBlockRepository availabilityBlockRepository,
                                         EmployeeRepository employeeRepository) {
        this.availabilityBlockRepository = availabilityBlockRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public List<AvailabilityBlock> execute(UUID employeeId) {
        // Validate employee exists
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + employeeId));

        return availabilityBlockRepository.findByEmployeeId(employeeId);
    }
}