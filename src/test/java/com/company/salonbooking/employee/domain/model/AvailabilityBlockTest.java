package com.company.salonbooking.employee.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AvailabilityBlockTest {

    @Test
    void deveRecusarStartAtAposEndAt() {
        Instant now = Instant.now();
        assertThrows(IllegalArgumentException.class, () ->
                AvailabilityBlock.create(UUID.randomUUID(), UUID.randomUUID(), now.plusSeconds(3600), now, "férias", now));
    }

    @Test
    void deveDetectarOverlap() {
        Instant now = Instant.now();
        AvailabilityBlock block = AvailabilityBlock.create(UUID.randomUUID(), UUID.randomUUID(),
                now.plusSeconds(3600), now.plusSeconds(7200), "almoço estendido", now);

        assertThat(block.overlaps(now.plusSeconds(5000), now.plusSeconds(9000))).isTrue();
        assertThat(block.overlaps(now.plusSeconds(8000), now.plusSeconds(9000))).isFalse();
    }
}