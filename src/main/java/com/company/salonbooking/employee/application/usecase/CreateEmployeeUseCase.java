package com.company.salonbooking.employee.application.usecase;

import com.company.salonbooking.audit.domain.model.AuditAction;
import com.company.salonbooking.business.domain.exception.BusinessNotFoundException;
import com.company.salonbooking.business.domain.model.Business;
import com.company.salonbooking.business.domain.repository.BusinessRepository;
import com.company.salonbooking.employee.application.command.CreateEmployeeCommand;
import com.company.salonbooking.employee.domain.model.Employee;
import com.company.salonbooking.employee.domain.repository.EmployeeRepository;
import com.company.salonbooking.identity.application.port.PasswordHasher;
import com.company.salonbooking.identity.application.port.TokenIssuer;
import com.company.salonbooking.identity.domain.exception.EmailAlreadyExistsException;
import com.company.salonbooking.identity.domain.model.Role;
import com.company.salonbooking.identity.domain.model.User;
import com.company.salonbooking.identity.domain.repository.UserRepository;
import com.company.salonbooking.shared.application.port.AuditRecorder;
import com.company.salonbooking.shared.exception.UnauthorizedResourceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/**
 * Creates both the identity User (role EMPLOYEE) and the Employee record atomically.
 * Cross-module dependency on identity's ports is a deliberate exception: employee
 * onboarding inherently spans both bounded contexts, and coupling by ID alone would
 * force this orchestration into the interfaces layer, leaking a domain workflow into REST.
 */
@Service
public class CreateEmployeeUseCase {

    private final BusinessRepository businessRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;
    private final AuditRecorder auditRecorder;
    private final Clock clock;

    public CreateEmployeeUseCase(BusinessRepository businessRepository, EmployeeRepository employeeRepository,
                                 UserRepository userRepository, PasswordHasher passwordHasher,
                                 TokenIssuer tokenIssuer, AuditRecorder auditRecorder, Clock clock) {
        this.businessRepository = businessRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
        this.auditRecorder = auditRecorder;
        this.clock = clock;
    }

    @Transactional
    public Employee execute(CreateEmployeeCommand command) {
        Business business = businessRepository.findById(command.businessId())
                .orElseThrow(() -> new BusinessNotFoundException(command.businessId()));

        if (!business.isOwnedBy(command.requesterId())) {
            throw new UnauthorizedResourceException("You do not own this business.");
        }

        String normalizedEmail = command.email().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        Instant now = Instant.now(clock);
        String hash = passwordHasher.hash(command.rawPassword());
        User user = User.register(UUID.randomUUID(), command.name(), normalizedEmail, hash, Role.EMPLOYEE, now);
        User savedUser = userRepository.save(user);

        Employee employee = Employee.create(UUID.randomUUID(), savedUser.getId(), business.getId(), command.specialty(), now);

        Employee saved = employeeRepository.save(employee);
        auditRecorder.record(command.requesterId(), business.getId(), AuditAction.CREATE_EMPLOYEE, "Employee",
                saved.getId(), null);

        return saved;
    }
}
