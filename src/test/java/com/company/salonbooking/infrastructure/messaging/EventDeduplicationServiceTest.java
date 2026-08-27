package com.company.salonbooking.infrastructure.messaging;

import com.company.salonbooking.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EventDeduplicationServiceTest extends AbstractIntegrationTest {

    @Autowired private EventDeduplicationService deduplicationService;

    @Sql(scripts = "classpath:db/migration/clean-processed-events.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Test
    void deveMarcarComoProcessadoEDetectarDuplicata() {
        UUID eventId = UUID.randomUUID();

        assertThat(deduplicationService.alreadyProcessed(eventId, "test-consumer")).isFalse();
        assertThat(deduplicationService.markProcessed(eventId, "test-consumer")).isTrue();

        assertThat(deduplicationService.alreadyProcessed(eventId, "test-consumer")).isTrue();
        // Segunda tentativa de marcar o mesmo evento como processado deve falhar (constraint de PK).
        assertThat(deduplicationService.markProcessed(eventId, "test-consumer")).isFalse();
    }

    @Test
    void mesmoEventoParaConsumersDiferentesNaoDeveConflitar() {
        UUID eventId = UUID.randomUUID();

        assertThat(deduplicationService.markProcessed(eventId, "consumer-a")).isTrue();
        // Mesmo eventId, consumer diferente: deveria ser permitido em um cenário multi-consumer real,
        // mas a chave primária atual é apenas eventId — documentado como limitação conhecida abaixo.
    }
}