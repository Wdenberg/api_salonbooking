package com.company.salonbooking.infrastructure.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
@ConditionalOnProperty(prefix = "app.outbox.cleanup", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OutboxCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(OutboxCleanupJob.class);

    private final OutboxEventJpaRepository outboxEventRepository;
    private final OutboxProperties.Cleanup cleanupConfig;

    public OutboxCleanupJob(OutboxEventJpaRepository outboxEventRepository, OutboxProperties properties) {
        this.outboxEventRepository = outboxEventRepository;
        this.cleanupConfig = properties.cleanup();
    }

    @Scheduled(cron = "${app.outbox.cleanup.schedule-cron:0 0 3 * * ?}")
    @Transactional
    public void cleanupOldEvents() {
        if (!cleanupConfig.enabled()) {
            log.debug("Outbox cleanup is disabled");
            return;
        }

        Instant cutoff = Instant.now().minusSeconds(cleanupConfig.retentionDays() * 24L * 60 * 60);

        log.info("Starting outbox cleanup: removing PUBLISHED events older than {}", cutoff);

        int deletedCount = outboxEventRepository.deletePublishedBefore(cutoff);

        log.info("Outbox cleanup completed: deleted {} events older than {}", deletedCount, cutoff);
    }
}