package com.company.salonbooking.infrastructure.outbox;

import com.company.salonbooking.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxCleanupJobIntegrationTest extends AbstractIntegrationTest {

    @Autowired private OutboxEventJpaRepository outboxRepository;
    @Autowired private OutboxCleanupJob outboxCleanupJob;

    @AfterEach
    void cleanupTestData() {
        outboxRepository.deleteAllInBatch();
    }

    @Test
    void deveDeletarEventosPublicadosAntigos() {
        Instant now = Instant.now();
        Instant oldTime = now.minus(60, ChronoUnit.DAYS);

        OutboxEventJpaEntity oldPublished = OutboxEventJpaEntity.create(
                UUID.randomUUID(), "Appointment", UUID.randomUUID(), "AppointmentCreated",
                "{\"test\":true}", oldTime);
        oldPublished.markPublished(oldTime.minusSeconds(10));
        outboxRepository.saveAndFlush(oldPublished);

        outboxCleanupJob.cleanupOldEvents();

        assertThat(outboxRepository.findById(oldPublished.getId())).isEmpty();
    }

    @Test
    void naoDeveDeletarEventosPendentes() {
        Instant now = Instant.now();
        Instant oldTime = now.minus(60, ChronoUnit.DAYS);

        OutboxEventJpaEntity oldPending = OutboxEventJpaEntity.create(
                UUID.randomUUID(), "Appointment", UUID.randomUUID(), "AppointmentCreated",
                "{\"test\":true}", oldTime);
        outboxRepository.saveAndFlush(oldPending);

        outboxCleanupJob.cleanupOldEvents();

        assertThat(outboxRepository.findById(oldPending.getId())).isPresent();
    }

    @Test
    void naoDeveDeletarEventosPublicadosRecentes() {
        Instant now = Instant.now();
        Instant recentTime = now.minus(10, ChronoUnit.DAYS);

        OutboxEventJpaEntity recentPublished = OutboxEventJpaEntity.create(
                UUID.randomUUID(), "Appointment", UUID.randomUUID(), "AppointmentCreated",
                "{\"test\":true}", recentTime);
        recentPublished.markPublished(recentTime.minusSeconds(10));
        outboxRepository.saveAndFlush(recentPublished);

        outboxCleanupJob.cleanupOldEvents();

        assertThat(outboxRepository.findById(recentPublished.getId())).isPresent();
    }
}
