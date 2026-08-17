package com.company.salonbooking.identity.application.usecase;

import com.company.salonbooking.identity.application.command.RegisterOwnerCommand;
import com.company.salonbooking.identity.application.dto.AuthResult;
import com.company.salonbooking.identity.application.port.PasswordHasher;
import com.company.salonbooking.identity.application.port.TokenIssuer;
import com.company.salonbooking.identity.domain.exception.EmailAlreadyExistsException;
import com.company.salonbooking.identity.domain.model.Role;
import com.company.salonbooking.identity.domain.model.User;
import com.company.salonbooking.identity.domain.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;


import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class RegisterOwnerUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;
    private final Clock clock;


    public RegisterOwnerUseCase(UserRepository userRepository, PasswordHasher passwordHasher, TokenIssuer tokenIssuer, Clock clock) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
        this.clock = clock;
    }

    @Transactional
    public AuthResult execute(RegisterOwnerCommand command){
        String normalizedEmail = command.email().toLowerCase();
        if(userRepository.existsByEmail((normalizedEmail))){
            throw new EmailAlreadyExistsException(normalizedEmail);
        }
        String hash = passwordHasher.hash(command.rawPassword());
        User user = User.register(UUID.randomUUID(), command.name(), normalizedEmail, hash, Role.OWNER, Instant.now(clock));
        User saved = userRepository.save(user);

        TokenIssuer.IssuedToken token = tokenIssuer.issueToken(saved);
        return new AuthResult(saved.getId(), token.accessToken(), token.expiresInSeconds());

    }
}
