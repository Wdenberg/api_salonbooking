package com.company.salonbooking.infrastructure.outbox;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

/**
 * Reports DOWN if there are PENDING outbox events significantly older than they
 * should be — a strong signal that the publisher job has stopped running or RabbitMQ
 * has been unreachable for a while, even if RabbitMQ's own connection health check
 * still reports UP (e.g. transient network partitions that heal before the next
 * Actuator poll).
 */
@Component
public class OutboxHealthIndicator implements HealthIndicator {

    private static final long STALE_THRESHOLD_SECONDS = 300; // 5 minutes

    private final OutboxEventJpaRepository repository;
    private final Clock clock;

    public OutboxHealthIndicator(OutboxEventJpaRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public Health health() {
        Instant staleThreshold = Instant.now(clock).minusSeconds(STALE_THRESHOLD_SECONDS);
        long stalePendingCount = repository.countStalePending(staleThreshold);

        if (stalePendingCount > 0) {
            return Health.down()
                    .withDetail("stalePendingEvents", stalePendingCount)
                    .withDetail("staleThresholdSeconds", STALE_THRESHOLD_SECONDS)
                    .build();
        }

        return Health.up().build();
    }
}