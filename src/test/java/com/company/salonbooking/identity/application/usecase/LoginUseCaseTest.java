package com.company.salonbooking.identity.application.usecase;

import com.company.salonbooking.identity.application.command.LoginCommand;
import com.company.salonbooking.identity.application.dto.AuthResult;
import com.company.salonbooking.identity.application.port.PasswordHasher;
import com.company.salonbooking.identity.application.port.TokenIssuer;
import com.company.salonbooking.identity.domain.exception.InvalidCredentialsException;
import com.company.salonbooking.identity.domain.model.Role;
import com.company.salonbooking.identity.domain.model.User;
import com.company.salonbooking.identity.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordHasher passwordHasher;
    @Mock private TokenIssuer tokenIssuer;

    private LoginUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new LoginUseCase(userRepository, passwordHasher, tokenIssuer);
    }

    @Test
    void deveRecusarUsuarioInexistente() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> useCase.execute(new LoginCommand("nobody@example.com", "any")));
    }

    @Test
    void deveRecusarSenhaIncorreta() {
        User user = User.register(UUID.randomUUID(), "Jane", "jane@example.com", "hash", Role.CUSTOMER, Instant.now());
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("wrong", "hash")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> useCase.execute(new LoginCommand("jane@example.com", "wrong")));
    }

    @Test
    void deveAutenticarEEmitirToken() {
        User user = User.register(UUID.randomUUID(), "Jane", "jane@example.com", "hash", Role.CUSTOMER, Instant.now());
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("correct", "hash")).thenReturn(true);
        when(tokenIssuer.issueToken(user)).thenReturn(new TokenIssuer.IssuedToken("jwt-token", 3600L));

        AuthResult result = useCase.execute(new LoginCommand("jane@example.com", "correct"));

        assertThat(result.accessToken()).isEqualTo("jwt-token");
    }
}