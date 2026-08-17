package com.company.salonbooking.identity.application.usecase;

import com.company.salonbooking.identity.application.command.LoginCommand;
import com.company.salonbooking.identity.application.dto.AuthResult;
import com.company.salonbooking.identity.application.port.PasswordHasher;
import com.company.salonbooking.identity.application.port.TokenIssuer;
import com.company.salonbooking.identity.domain.exception.InvalidCredentialsException;
import com.company.salonbooking.identity.domain.model.User;
import com.company.salonbooking.identity.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;

    public LoginUseCase(UserRepository userRepository, PasswordHasher passwordHasher, TokenIssuer tokenIssuer) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
    }

    @Transactional(readOnly = true)
    public AuthResult execute(LoginCommand command) {
        String normalizedEmail = command.email().toLowerCase();

        // Deliberately generic exception for both "no such user" and "wrong password"
        // to avoid leaking which case occurred (prevents user enumeration).
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordHasher.matches(command.rawPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        if (!user.isActive()) {
            throw new InvalidCredentialsException();
        }

        TokenIssuer.IssuedToken token = tokenIssuer.issueToken(user);
        return new AuthResult(user.getId(), token.accessToken(), token.expiresInSeconds());
    }
}