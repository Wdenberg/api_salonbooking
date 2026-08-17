package com.company.salonbooking.identity.application.usecase;

import com.company.salonbooking.identity.application.command.RegisterOwnerCommand;
import com.company.salonbooking.identity.application.dto.AuthResult;
import com.company.salonbooking.identity.application.port.PasswordHasher;
import com.company.salonbooking.identity.application.port.TokenIssuer;
import com.company.salonbooking.identity.domain.exception.EmailAlreadyExistsException;
import com.company.salonbooking.identity.domain.model.Role;
import com.company.salonbooking.identity.domain.model.User;
import com.company.salonbooking.identity.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterOwnerUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordHasher passwordHasher;
    @Mock private TokenIssuer tokenIssuer;

    private RegisterOwnerUseCase useCase;
    private final Clock clock = Clock.fixed(Instant.parse("2026-08-12T10:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        useCase = new RegisterOwnerUseCase(userRepository, passwordHasher, tokenIssuer, clock);
    }

    @Test
    void deveRecusarEmailDuplicado() {
        when(userRepository.existsByEmail("owner@example.com")).thenReturn(true);

        RegisterOwnerCommand command = new RegisterOwnerCommand("Owner", "owner@example.com", "password123");

        assertThrows(EmailAlreadyExistsException.class, () -> useCase.execute(command));
    }

    @Test
    void deveRegistrarOwnerEEmitirToken() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordHasher.hash("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenIssuer.issueToken(any(User.class))).thenReturn(new TokenIssuer.IssuedToken("jwt-token", 3600L));

        RegisterOwnerCommand command = new RegisterOwnerCommand("Owner", "owner@example.com", "password123");
        AuthResult result = useCase.execute(command);

        assertThat(result.accessToken()).isEqualTo("jwt-token");
        assertThat(result.expiresInSeconds()).isEqualTo(3600L);
    }
}