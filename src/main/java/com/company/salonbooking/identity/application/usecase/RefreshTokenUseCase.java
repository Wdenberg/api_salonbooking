package com.company.salonbooking.identity.application.usecase;

import com.company.salonbooking.audit.domain.model.AuditAction;
import com.company.salonbooking.identity.application.command.RefreshTokenCommand;
import com.company.salonbooking.identity.application.dto.AuthResult;
import com.company.salonbooking.identity.application.port.RefreshTokenHasher;
import com.company.salonbooking.identity.application.port.TokenIssuer;
import com.company.salonbooking.identity.domain.exception.InvalidRefreshTokenException;
import com.company.salonbooking.identity.domain.exception.RefreshTokenReuseDetectedException;
import com.company.salonbooking.identity.domain.model.RefreshToken;
import com.company.salonbooking.identity.domain.model.User;
import com.company.salonbooking.identity.domain.repository.RefreshTokenRepository;
import com.company.salonbooking.identity.domain.repository.UserRepository;
import com.company.salonbooking.shared.application.port.AuditRecorder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final RefreshTokenHasher tokenHasher;
    private final TokenIssuer tokenIssuer;
    private final AuditRecorder auditRecorder;
    private final Clock clock;

    public RefreshTokenUseCase(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository,
                               RefreshTokenHasher tokenHasher, TokenIssuer tokenIssuer,
                               AuditRecorder auditRecorder, Clock clock) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.tokenHasher = tokenHasher;
        this.tokenIssuer = tokenIssuer;
        this.auditRecorder = auditRecorder;
        this.clock = clock;
    }

    @Transactional
    public AuthResult execute(RefreshTokenCommand command) {
        String tokenHash = tokenHasher.hash(command.refreshToken());

        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(InvalidRefreshTokenException::new);

        Instant now = Instant.now(clock);

        if (storedToken.isRevoked()) {
            // Check for reuse detection: if this token was revoked due to rotation reuse,
            // we should revoke the entire chain (all tokens with same parent)
            if ("ROTATION_REUSE_DETECTED".equals(storedToken.getRevokedReason())) {
                revokeTokenChain(storedToken.getUserId(), now);
            }
            throw new InvalidRefreshTokenException();
        }

        if (storedToken.isExpired(now)) {
            throw new InvalidRefreshTokenException();
        }

        User user = userRepository.findById(storedToken.getUserId())
                .orElseThrow(InvalidRefreshTokenException::new);

        if (!user.isActive()) {
            throw new InvalidRefreshTokenException();
        }

        // Rotate: revoke current token, create new one with parent link
        storedToken.revoke("ROTATION", now);
        refreshTokenRepository.save(storedToken);

        TokenIssuer.IssuedToken tokens = tokenIssuer.issueToken(user);
        String newRefreshTokenHash = tokenHasher.hash(tokens.refreshToken());

        RefreshToken newToken = RefreshToken.create(
                UUID.randomUUID(),
                user.getId(),
                newRefreshTokenHash,
                tokenHash,
                now.plusSeconds(tokens.refreshTokenExpiresInSeconds()),
                now
        );
        refreshTokenRepository.save(newToken);

        auditRecorder.record(user.getId(), null, AuditAction.TOKEN_REFRESH, "User", user.getId(), null);

        return new AuthResult(user.getId(), tokens.accessToken(), tokens.refreshToken(),
                tokens.accessTokenExpiresInSeconds(), tokens.refreshTokenExpiresInSeconds());
    }

    private void revokeTokenChain(UUID userId, Instant now) {
        // Revoke all active tokens for this user (security measure for detected reuse)
        refreshTokenRepository.revokeAllByUserId(userId, now, "ROTATION_REUSE_DETECTED");
    }
}