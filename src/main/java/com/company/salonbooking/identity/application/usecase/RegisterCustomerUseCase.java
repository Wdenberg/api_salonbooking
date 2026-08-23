package com.company.salonbooking.identity.application.usecase;

import com.company.salonbooking.identity.application.command.RegisterCustomerCommand;
import com.company.salonbooking.identity.application.dto.AuthResult;
import com.company.salonbooking.identity.application.port.CustomerProfileInitializer;
import com.company.salonbooking.identity.application.port.PasswordHasher;
import com.company.salonbooking.identity.application.port.TokenIssuer;
import com.company.salonbooking.identity.domain.exception.EmailAlreadyExistsException;
import com.company.salonbooking.identity.domain.model.Role;
import com.company.salonbooking.identity.domain.model.User;
import com.company.salonbooking.identity.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class RegisterCustomerUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;
    private final CustomerProfileInitializer customerProfileInitializer;
    private final Clock clock;

    public RegisterCustomerUseCase(UserRepository userRepository, PasswordHasher passwordHasher, TokenIssuer tokenIssuer,
                                   CustomerProfileInitializer customerProfileInitializer, Clock clock) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
        this.customerProfileInitializer = customerProfileInitializer;
        this.clock = clock;
    }

    @Transactional
    public AuthResult execute(RegisterCustomerCommand command) {
        String normalizedEmail = command.email().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        String hash = passwordHasher.hash(command.rawPassword());
        User user = User.register(UUID.randomUUID(), command.name(), normalizedEmail, hash, Role.CUSTOMER, Instant.now(clock));
        User saved = userRepository.save(user);

        customerProfileInitializer.initializeFor(saved.getId());

        TokenIssuer.IssuedToken token = tokenIssuer.issueToken(saved);
        return new AuthResult(saved.getId(), token.accessToken(), token.expiresInSeconds());
    }
}