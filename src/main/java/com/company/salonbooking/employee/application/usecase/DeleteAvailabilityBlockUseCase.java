package com.company.salonbooking.employee.application.usecase;

import com.company.salonbooking.business.domain.repository.BusinessRepository;
import com.company.salonbooking.employee.application.command.DeleteAvailabilityBlockCommand;
import com.company.salonbooking.employee.domain.exception.EmployeeNotFoundException;
import com.company.salonbooking.employee.domain.model.AvailabilityBlock;
import com.company.salonbooking.employee.domain.model.Employee;
import com.company.salonbooking.employee.domain.repository.AvailabilityBlockRepository;
import com.company.salonbooking.employee.domain.repository.EmployeeRepository;
import com.company.salonbooking.shared.exception.ResourceNotFoundException;
import com.company.salonbooking.shared.exception.UnauthorizedResourceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteAvailabilityBlockUseCase {

    private final AvailabilityBlockRepository blockRepository;
    private final EmployeeRepository employeeRepository;
    private final BusinessRepository businessRepository;

    public DeleteAvailabilityBlockUseCase(AvailabilityBlockRepository blockRepository, EmployeeRepository employeeRepository,
                                          BusinessRepository businessRepository) {
        this.blockRepository = blockRepository;
        this.employeeRepository = employeeRepository;
        this.businessRepository = businessRepository;
    }

    @Transactional
    public void execute(DeleteAvailabilityBlockCommand command) {
        AvailabilityBlock block = blockRepository.findById(command.blockId())
                .orElseThrow(() -> new ResourceNotFoundException("Availability block not found: " + command.blockId()));

        Employee employee = employeeRepository.findById(block.getEmployeeId())
                .orElseThrow(() -> new EmployeeNotFoundException(block.getEmployeeId()));

        boolean isSelf = employee.isUser(command.requesterId());
        boolean isOwner = businessRepository.findById(employee.getBusinessId())
                .map(b -> b.isOwnedBy(command.requesterId()))
                .orElse(false);

        if (!isSelf && !isOwner) {
            throw new UnauthorizedResourceException("You cannot delete this availability block.");
        }

        blockRepository.deleteById(block.getId());
    }
}