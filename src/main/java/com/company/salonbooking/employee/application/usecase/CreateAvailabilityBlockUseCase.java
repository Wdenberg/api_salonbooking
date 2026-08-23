package com.company.salonbooking.employee.application.usecase;

import com.company.salonbooking.business.domain.repository.BusinessRepository;
import com.company.salonbooking.employee.application.command.CreateAvailabilityBlockCommand;
import com.company.salonbooking.employee.domain.exception.EmployeeNotFoundException;
import com.company.salonbooking.employee.domain.exception.InvalidAvailabilityBlockException;
import com.company.salonbooking.employee.domain.model.AvailabilityBlock;
import com.company.salonbooking.employee.domain.model.Employee;
import com.company.salonbooking.employee.domain.repository.AvailabilityBlockRepository;
import com.company.salonbooking.employee.domain.repository.EmployeeRepository;
import com.company.salonbooking.shared.exception.UnauthorizedResourceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class CreateAvailabilityBlockUseCase {

    private final EmployeeRepository employeeRepository;
    private final AvailabilityBlockRepository blockRepository;
    private final BusinessRepository businessRepository;
    private final Clock clock;

    public CreateAvailabilityBlockUseCase(EmployeeRepository employeeRepository, AvailabilityBlockRepository blockRepository,
                                          BusinessRepository businessRepository, Clock clock) {
        this.employeeRepository = employeeRepository;
        this.blockRepository = blockRepository;
        this.businessRepository = businessRepository;
        this.clock = clock;
    }

    @Transactional
    public AvailabilityBlock execute(CreateAvailabilityBlockCommand command) {
        Employee employee = employeeRepository.findById(command.employeeId())
                .orElseThrow(() -> new EmployeeNotFoundException(command.employeeId()));

        boolean isSelf = employee.isUser(command.requesterId());
        boolean isOwner = businessRepository.findById(employee.getBusinessId())
                .map(b -> b.isOwnedBy(command.requesterId()))
                .orElse(false);

        if (!isSelf && !isOwner) {
            throw new UnauthorizedResourceException("You cannot create availability blocks for this employee.");
        }

        if (!command.startAt().isAfter(Instant.now(clock))) {
            throw new InvalidAvailabilityBlockException("Availability block must start in the future.");
        }

        AvailabilityBlock block = AvailabilityBlock.create(UUID.randomUUID(), command.employeeId(),
                command.startAt(), command.endAt(), command.reason(), Instant.now(clock));

        return blockRepository.save(block);
    }
}