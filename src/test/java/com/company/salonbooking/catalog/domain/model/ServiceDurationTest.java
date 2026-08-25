package com.company.salonbooking.catalog.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServiceDurationTest {

    @Test
    void deveRecusarDuracaoZeroOuNegativa() {
        assertThrows(IllegalArgumentException.class, () -> ServiceDuration.ofMinutes(0));
        assertThrows(IllegalArgumentException.class, () -> ServiceDuration.ofMinutes(-10));
    }

    @Test
    void deveConverterParaJavaDuration() {
        ServiceDuration duration = ServiceDuration.ofMinutes(45);

        assertThat(duration.asJavaDuration().toMinutes()).isEqualTo(45);
    }
}