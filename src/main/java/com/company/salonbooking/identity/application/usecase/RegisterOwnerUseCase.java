package com.company.salonbooking.identity.application.usecase;

import com.company.salonbooking.identity.application.command.RegisterOwnerCommand;
import com.company.salonbooking.identity.application.dto.AuthResult;
import com.company.salonbooking.identity.application.port.PasswordHasher;
import com.company.salonbooking.identity.application.port.RefreshTokenHasher;
import com.company.salonbooking.identity.application.port.TokenIssuer;
import com.company.salonbooking.identity.domain.exception.EmailAlreadyExistsException;
import com.company.salonbooking.identity.domain.model.RefreshToken;
import com.company.salonbooking.identity.domain.model.Role;
import com.company.salonbooking.identity.domain.model.User;
import com.company.salonbooking.identity.domain.repository.RefreshTokenRepository;
import com.company.salonbooking.identity.domain.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;


@Service
public class RegisterOwnerUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordHasher passwordHasher;
    private final RefreshTokenHasher refreshTokenHasher;
    private final TokenIssuer tokenIssuer;
    private final Clock clock;


    public RegisterOwnerUseCase(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
                                PasswordHasher passwordHasher, RefreshTokenHasher refreshTokenHasher,
                                TokenIssuer tokenIssuer, Clock clock) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordHasher = passwordHasher;
        this.refreshTokenHasher = refreshTokenHasher;
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

        Instant now = Instant.now(clock);
        TokenIssuer.IssuedToken token = tokenIssuer.issueToken(saved);

        String refreshTokenHash = refreshTokenHasher.hash(token.refreshToken());
        RefreshToken refreshToken = RefreshToken.create(
                UUID.randomUUID(), saved.getId(), refreshTokenHash, null,
                now.plusSeconds(token.refreshTokenExpiresInSeconds()), now);
        refreshTokenRepository.save(refreshToken);

        return new AuthResult(saved.getId(), token.accessToken(), token.refreshToken(), token.accessTokenExpiresInSeconds(), token.refreshTokenExpiresInSeconds());

    }
}