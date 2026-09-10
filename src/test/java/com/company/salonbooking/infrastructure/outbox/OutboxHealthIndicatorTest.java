package com.company.salonbooking.infrastructure.outbox;

import com.company.salonbooking.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.contributor.Status;


import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxHealthIndicatorTest extends AbstractIntegrationTest {

    @Autowired private OutboxEventJpaRepository repository;
    @Autowired private OutboxHealthIndicator healthIndicator;
    @Autowired private Clock clock;

    @BeforeEach
    void cleanOutbox() {
        repository.deleteAll();
        repository.flush();
    }

    @Test
    void deveReportarUpQuandoNaoHaEventosAntigosPendentes() {
        var health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void deveReportarDownQuandoExisteEventoPendenteAntigo() {
        Instant old = Instant.now(clock).minusSeconds(600); // 10 minutes ago, older than the 5-minute threshold
        OutboxEventJpaEntity stale = OutboxEventJpaEntity.create(
                UUID.randomUUID(), "Appointment", UUID.randomUUID(), "AppointmentCreated", "{}", old);
        repository.saveAndFlush(stale);

        var health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsKey("stalePendingEvents");
    }
}
