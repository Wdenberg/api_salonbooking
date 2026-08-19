package com.company.salonbooking.business.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessTest {

    @Test
    void deveCriarBusinessAtivo() {
        UUID ownerId = UUID.randomUUID();
        Business business = Business.create(UUID.randomUUID(), ownerId, "Barbearia do Zé", "desc", "119999",
                "ze@example.com", Address.empty(), ZoneId.of("America/Recife"), Instant.now());

        assertThat(business.isActive()).isTrue();
        assertThat(business.isOwnedBy(ownerId)).isTrue();
    }

    @Test
    void deveIdentificarNaoDono() {
        Business business = Business.create(UUID.randomUUID(), UUID.randomUUID(), "Salão X", null, null, null,
                Address.empty(), ZoneId.of("UTC"), Instant.now());

        assertThat(business.isOwnedBy(UUID.randomUUID())).isFalse();
    }
}