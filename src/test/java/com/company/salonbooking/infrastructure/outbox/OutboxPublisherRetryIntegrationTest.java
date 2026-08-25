package com.company.salonbooking.infrastructure.outbox;

import com.company.salonbooking.TestClockConfig;
import com.company.salonbooking.infrastructure.messaging.MessageBroker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

@Import(TestClockConfig.class)
class OutboxPublisherRetryIntegrationTest extends com.company.salonbooking.AbstractIntegrationTest {

    @Autowired private OutboxEventJpaRepository repository;
    @Autowired private OutboxPublisherJob outboxPublisherJob;
    @Autowired private Clock clock;

   @MockitoBean
    private MessageBroker messageBroker;

    @Test
    @Transactional
    void eventoDeveSerReagendadoAposFalhaEMarcadoFailedAposEsgotarTentativas() {
        doThrow(new RuntimeException("broker unavailable")).when(messageBroker)
                .publish(anyString(), any(UUID.class), anyString());

        MutableClock mutableClock = (MutableClock) clock;
        // O clock já foi inicializado com Instant.now() no bean @Primary
        OutboxEventJpaEntity entity = OutboxEventJpaEntity.create(
                UUID.randomUUID(), "Appointment", UUID.randomUUID(), "AppointmentCreated", "{}", mutableClock.instant());
        repository.saveAndFlush(entity);

        // maxAttempts = 5 (configurado em application.yml)
        for (int i = 0; i < 5; i++) {
            outboxPublisherJob.dispatchBatch();
            // Avança o relógio em 10 minutos para ultrapassar o backoff máximo (5 minutos)
            mutableClock.advance(Duration.ofMinutes(10));
        }

        OutboxEventJpaEntity result = repository.findById(entity.getId()).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(result.getAttempts()).isEqualTo(5);
    }
}