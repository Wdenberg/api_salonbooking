package com.company.salonbooking.identity.domain.model;


import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-08-12T10:00:00Z"), java.time.ZoneOffset.UTC);

    @Test
    void deveRegistrarUsuarioAtivoComRoleInicial() {
        User user = User.register(UUID.randomUUID(), "Jane Doe", "JANE@Example.com", "hash", Role.OWNER, Instant.now(clock));

        assertThat(user.isActive()).isTrue();
        assertThat(user.hasRole(Role.OWNER)).isTrue();
        assertThat(user.getEmail()).isEqualTo("jane@example.com"); // normalizado
    }

    @Test
    void deveBloquearUsuario() {
        User user = User.register(UUID.randomUUID(), "Jane Doe", "jane@example.com", "hash", Role.OWNER, Instant.now(clock));

        user.block(Instant.now(clock).plusSeconds(60));

        assertThat(user.isActive()).isFalse();
        assertThat(user.getStatus()).isEqualTo(UserStatus.BLOCKED);
    }

    @Test
    void deveRecusarNomeEmBranco() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () ->
                User.register(UUID.randomUUID(), "  ", "jane@example.com", "hash", Role.OWNER, Instant.now(clock)));
    }
}