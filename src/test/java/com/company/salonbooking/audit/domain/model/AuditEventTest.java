package com.company.salonbooking.audit.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuditEventTest {

    @Test
    void deveCriarEventoComBusinessIdNulo() {
        AuditEvent event = AuditEvent.record(UUID.randomUUID(), UUID.randomUUID(), null, AuditAction.LOGIN,
                "User", UUID.randomUUID(), null, "127.0.0.1", "JUnit", Instant.now());

        assertThat(event.getBusinessId()).isNull();
        assertThat(event.getAction()).isEqualTo(AuditAction.LOGIN);
    }
}
