package com.company.salonbooking.audit.interfaces.rest.dto;

import com.company.salonbooking.audit.domain.model.AuditAction;
import com.company.salonbooking.audit.domain.model.AuditEvent;

import java.time.Instant;
import java.util.UUID;

public record AuditEventResponse(
        UUID id, UUID actorUserId, UUID businessId, AuditAction action, String resourceType, UUID resourceId,
        String metadata, String ipAddress, Instant occurredAt
) {
    public static AuditEventResponse from(AuditEvent event) {
        return new AuditEventResponse(event.getId(), event.getActorUserId(), event.getBusinessId(), event.getAction(),
                event.getResourceType(), event.getResourceId(), event.getMetadata(), event.getIpAddress(), event.getOccurredAt());
        // Note: userAgent intentionally omitted from the response DTO — not useful to
        // the OWNER reviewing their audit log, and keeps the payload lean.
    }
}
