package com.company.salonbooking.identity.application.usecase;

import com.company.salonbooking.audit.domain.model.AuditAction;
import com.company.salonbooking.identity.application.command.LogoutCommand;
import com.company.salonbooking.identity.application.port.AccessTokenBlocklist;
import com.company.salonbooking.identity.application.port.AccessTokenUserIdParser;
import com.company.salonbooking.identity.application.port.RefreshTokenHasher;
import com.company.salonbooking.identity.domain.model.RefreshToken;
import com.company.salonbooking.identity.domain.repository.RefreshTokenRepository;
import com.company.salonbooking.shared.application.port.AuditRecorder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class LogoutUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenHasher tokenHasher;
    private final AccessTokenBlocklist accessTokenBlocklist;
    private final AccessTokenUserIdParser accessTokenUserIdParser;
    private final AuditRecorder auditRecorder;
    private final Clock clock;

    public LogoutUseCase(RefreshTokenRepository refreshTokenRepository, RefreshTokenHasher tokenHasher,
                         AccessTokenBlocklist accessTokenBlocklist, AccessTokenUserIdParser accessTokenUserIdParser,
                         AuditRecorder auditRecorder, Clock clock) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenHasher = tokenHasher;
        this.accessTokenBlocklist = accessTokenBlocklist;
        this.accessTokenUserIdParser = accessTokenUserIdParser;
        this.auditRecorder = auditRecorder;
        this.clock = clock;
    }

    @Transactional
    public void execute(LogoutCommand command) {
        Instant now = Instant.now(clock);
        UUID userId = extractUserId(command.accessToken()).orElse(null);

        // Revoke refresh token if provided
        if (command.refreshToken() != null && !command.refreshToken().isBlank()) {
            String tokenHash = tokenHasher.hash(command.refreshToken());
            refreshTokenRepository.findByTokenHash(tokenHash)
                    .ifPresent(token -> {
                        if (token.isValid(now)) {
                            token.revoke("LOGOUT", now);
                            refreshTokenRepository.save(token);
                        }
                    });
        }

        // Add access token to blocklist if provided
        if (command.accessToken() != null && !command.accessToken().isBlank()) {
            accessTokenBlocklist.add(command.accessToken(), now);
        }

        // Audit
        if (userId != null) {
            auditRecorder.record(userId, null, AuditAction.LOGOUT, "User", userId, null);
        }
    }

    private java.util.Optional<UUID> extractUserId(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return java.util.Optional.empty();
        }
        return accessTokenUserIdParser.extractUserId(accessToken);
    }
}