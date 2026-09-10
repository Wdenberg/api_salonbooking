package com.company.salonbooking.infrastructure.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AppMetricsTest {

    @Test
    void deveIncrementarContadoresIndependentemente() {
        var registry = new SimpleMeterRegistry();
        AppMetrics metrics = new AppMetrics(registry);

        metrics.incrementAppointmentCreated();
        metrics.incrementAppointmentCreated();
        metrics.incrementAppointmentConflict();

        assertThat(registry.get("appointments.created").counter().count()).isEqualTo(2.0);
        assertThat(registry.get("appointments.conflicts").counter().count()).isEqualTo(1.0);
        assertThat(registry.get("appointments.cancelled").counter().count()).isEqualTo(0.0);
    }
}