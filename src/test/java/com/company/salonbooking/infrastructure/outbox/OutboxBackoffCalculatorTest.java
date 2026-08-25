package com.company.salonbooking.infrastructure.outbox;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxBackoffCalculatorTest {

    @Test
    void primeiraTentativaUsaBackoffInicial() {
        long backoff = OutboxBackoffCalculator.computeBackoffSeconds(1, 5, 300);
        assertThat(backoff).isEqualTo(5);
    }

    @Test
    void backoffDobraACadaTentativa() {
        assertThat(OutboxBackoffCalculator.computeBackoffSeconds(1, 5, 300)).isEqualTo(5);
        assertThat(OutboxBackoffCalculator.computeBackoffSeconds(2, 5, 300)).isEqualTo(10);
        assertThat(OutboxBackoffCalculator.computeBackoffSeconds(3, 5, 300)).isEqualTo(20);
    }

    @Test
    void backoffNuncaUltrapassaOMaximo() {
        long backoff = OutboxBackoffCalculator.computeBackoffSeconds(20, 5, 300);
        assertThat(backoff).isEqualTo(300);
    }
}