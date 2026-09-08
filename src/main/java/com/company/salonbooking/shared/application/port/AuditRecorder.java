package com.company.salonbooking.shared.application.port;

import com.company.salonbooking.audit.domain.model.AuditAction;

import java.util.UUID;

/**
 * The ONLY way use cases record an audit trail. Deliberately does not accept ipAddress
 * or userAgent as parameters — the adapter implementation pulls those from the current
 * HTTP request via RequestContextHolder (Seção 60/61's correlation pattern), so use
 * cases stay free of any HTTP/servlet concern (Seção 12: domain/application must not
 * depend on HTTP).
 *
 * Like OutboxDomainEventPublisherAdapter (Fase 8), the implementation writes within the
 * caller's existing transaction — an audited action and its audit row commit together.
 */
public interface AuditRecorder {

    void record(UUID actorUserId, UUID businessId, AuditAction action, String resourceType, UUID resourceId, String metadata);
}