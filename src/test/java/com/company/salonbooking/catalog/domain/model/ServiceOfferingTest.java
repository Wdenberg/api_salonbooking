package com.company.salonbooking.catalog.domain.model;

import com.company.salonbooking.shared.domain.model.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceOfferingTest {

    @Test
    void deveCriarServicoAtivoPorPadrao() {
        ServiceOffering service = ServiceOffering.create(UUID.randomUUID(), UUID.randomUUID(), "Corte", "Corte masculino",
                Money.of(new BigDecimal("40"), "BRL"), ServiceDuration.ofMinutes(30), Instant.now());

        assertThat(service.isActive()).isTrue();
    }

    @Test
    void deveAtualizarPrecoEDuracaoPreservandoIdentidade() {
        ServiceOffering service = ServiceOffering.create(UUID.randomUUID(), UUID.randomUUID(), "Corte", null,
                Money.of(new BigDecimal("40"), "BRL"), ServiceDuration.ofMinutes(30), Instant.now());

        service.update("Corte Premium", "Com toalha quente", Money.of(new BigDecimal("60"), "BRL"),
                ServiceDuration.ofMinutes(45), Instant.now());

        assertThat(service.getName()).isEqualTo("Corte Premium");
        assertThat(service.getPrice().getAmount()).isEqualByComparingTo("60.00");
        assertThat(service.getDuration().toMinutes()).isEqualTo(45);
    }

    @Test
    void deveDesativarEReativarServico() {
        ServiceOffering service = ServiceOffering.create(UUID.randomUUID(), UUID.randomUUID(), "Corte", null,
                Money.of(new BigDecimal("40"), "BRL"), ServiceDuration.ofMinutes(30), Instant.now());

        service.deactivate(Instant.now());
        assertThat(service.isActive()).isFalse();

        service.activate(Instant.now());
        assertThat(service.isActive()).isTrue();
    }
}