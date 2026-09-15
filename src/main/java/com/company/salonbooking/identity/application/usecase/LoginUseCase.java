package com.company.salonbooking.identity.application.usecase;

import com.company.salonbooking.audit.domain.model.AuditAction;
import com.company.salonbooking.identity.application.command.LoginCommand;
import com.company.salonbooking.identity.application.dto.AuthResult;
import com.company.salonbooking.identity.application.port.FailedLoginTracker;
import com.company.salonbooking.identity.application.port.PasswordHasher;
import com.company.salonbooking.identity.application.port.TokenIssuer;
import com.company.salonbooking.identity.domain.exception.InvalidCredentialsException;
import com.company.salonbooking.identity.domain.exception.AccountLockedException;
import com.company.salonbooking.identity.domain.model.User;
import com.company.salonbooking.identity.domain.repository.UserRepository;
import com.company.salonbooking.shared.application.port.AuditRecorder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;
    private final AuditRecorder auditRecorder;
    private final FailedLoginTracker failedLoginTracker;

    public LoginUseCase(UserRepository userRepository, PasswordHasher passwordHasher, TokenIssuer tokenIssuer,
                        AuditRecorder auditRecorder, FailedLoginTracker failedLoginTracker
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
        this.auditRecorder = auditRecorder;
        this.failedLoginTracker = failedLoginTracker;
    }

    @Transactional
    public AuthResult execute(LoginCommand command) {
        String normalizedEmail = command.email().toLowerCase();
        String trackerKey = "login:" + normalizedEmail;

        // Check if account/IP is locked
        if (failedLoginTracker.isLocked(trackerKey)) {
            throw new AccountLockedException("Too many failed login attempts. Try again in 15 minutes.");
        }

        // Deliberately generic exception for both "no such user" and "wrong password"
        // to avoid leaking which case occurred (prevents user enumeration).
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> {
                    failedLoginTracker.recordFailure(trackerKey);
                    return new InvalidCredentialsException();
                });

        if (!passwordHasher.matches(command.rawPassword(), user.getPasswordHash())) {
            failedLoginTracker.recordFailure(trackerKey);
            throw new InvalidCredentialsException();
        }

        if (!user.isActive()) {
            failedLoginTracker.recordFailure(trackerKey);
            throw new InvalidCredentialsException();
        }

        // Success - reset failure count
        failedLoginTracker.recordSuccess(trackerKey);

        TokenIssuer.IssuedToken token = tokenIssuer.issueToken(user);
        auditRecorder.record(user.getId(), null, AuditAction.LOGIN, "User", user.getId(), null);
        return new AuthResult(user.getId(), token.accessToken(), token.refreshToken(), token.accessTokenExpiresInSeconds(), token.refreshTokenExpiresInSeconds());
    }
}