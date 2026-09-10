package com.company.salonbooking.audit.infrastructure;

import com.company.salonbooking.audit.domain.model.AuditAction;
import com.company.salonbooking.audit.domain.model.AuditEvent;
import com.company.salonbooking.audit.domain.repository.AuditEventRepository;
import com.company.salonbooking.infrastructure.web.RequestMetadataHolder;
import com.company.salonbooking.shared.application.port.AuditRecorder;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/**
 * Writes plainly via repository.save() within the caller's existing transaction —
 * same non-@Transactional-here pattern as OutboxDomainEventPublisherAdapter (Fase 8),
 * so an audited business action and its audit row always commit or roll back together.
 */
@Component
public class AuditRecorderAdapter implements AuditRecorder {

    private final AuditEventRepository repository;
    private final RequestMetadataHolder requestMetadataHolder;
    private final Clock clock;

    public AuditRecorderAdapter(AuditEventRepository repository, RequestMetadataHolder requestMetadataHolder, Clock clock) {
        this.repository = repository;
        this.requestMetadataHolder = requestMetadataHolder;
        this.clock = clock;
    }

    @Override
    public void record(UUID actorUserId, UUID businessId, AuditAction action, String resourceType, UUID resourceId, String metadata) {
        Instant now = Instant.now(clock);
        String ip = requestMetadataHolder.currentIpAddress().orElse(null);
        String userAgent = requestMetadataHolder.currentUserAgent().orElse(null);

        AuditEvent event = AuditEvent.record(UUID.randomUUID(), actorUserId, businessId, action, resourceType,
                resourceId, metadata, ip, userAgent, now);

        repository.save(event);
    }
}
