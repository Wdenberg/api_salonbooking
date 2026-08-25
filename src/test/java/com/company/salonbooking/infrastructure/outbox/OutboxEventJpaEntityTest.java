package com.company.salonbooking.infrastructure.outbox;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxEventJpaEntityTest {

    @Test
    void deveMarcarComoPublicado() {
        Instant now = Instant.now();
        OutboxEventJpaEntity entity = OutboxEventJpaEntity.create(UUID.randomUUID(), "Appointment", UUID.randomUUID(),
                "AppointmentCreated", "{}", now);

        entity.markPublished(now.plusSeconds(1));

        assertThat(entity.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
        assertThat(entity.getProcessedAt()).isNotNull();
    }

    @Test
    void deveReagendarComBackoffQuandoAbaixoDoMaximo() {
        Instant now = Instant.now();
        OutboxEventJpaEntity entity = OutboxEventJpaEntity.create(UUID.randomUUID(), "Appointment", UUID.randomUUID(),
                "AppointmentCreated", "{}", now);

        entity.registerFailure(now, 30, 5);

        assertThat(entity.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(entity.getAttempts()).isEqualTo(1);
        assertThat(entity.getNextAttemptAt()).isEqualTo(now.plusSeconds(30));
    }

    @Test
    void deveMarcarComoFailedAoAtingirMaximoDeTentativas() {
        Instant now = Instant.now();
        OutboxEventJpaEntity entity = OutboxEventJpaEntity.create(UUID.randomUUID(), "Appointment", UUID.randomUUID(),
                "AppointmentCreated", "{}", now);

        for (int i = 0; i < 4; i++) {
            entity.registerFailure(now, 5, 5);
        }
        assertThat(entity.getStatus()).isEqualTo(OutboxStatus.PENDING);

        entity.registerFailure(now, 5, 5); // 5th attempt reaches maxAttempts

        assertThat(entity.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(entity.getAttempts()).isEqualTo(5);
    }
}