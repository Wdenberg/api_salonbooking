package com.company.salonbooking.audit.interfaces.rest.dto;

import com.company.salonbooking.audit.domain.model.AuditAction;
import com.company.salonbooking.audit.domain.model.AuditEvent;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record AuditEventResponse(
        @Schema(description = "Audit event ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
        @Schema(description = "Actor user ID", example = "550e8400-e29b-41d4-a716-446655440001")
        UUID actorUserId,
        @Schema(description = "Business ID", example = "550e8400-e29b-41d4-a716-446655440002")
        UUID businessId,
        @Schema(description = "Audit action", example = "APPOINTMENT_CREATED")
        AuditAction action,
        @Schema(description = "Resource type", example = "APPOINTMENT")
        String resourceType,
        @Schema(description = "Resource ID", example = "550e8400-e29b-41d4-a716-446655440003")
        UUID resourceId,
        @Schema(description = "Additional metadata (JSON)", example = "{\"serviceId\": \"...\"}")
        String metadata,
        @Schema(description = "Actor IP address", example = "192.168.1.1")
        String ipAddress,
        @Schema(description = "Event timestamp", example = "2026-09-16T10:00:00Z")
        Instant occurredAt
) {
    public static AuditEventResponse from(AuditEvent event) {
        return new AuditEventResponse(event.getId(), event.getActorUserId(), event.getBusinessId(), event.getAction(),
                event.getResourceType(), event.getResourceId(), event.getMetadata(), event.getIpAddress(), event.getOccurredAt());
        // Note: userAgent intentionally omitted from the response DTO — not useful to
        // the OWNER reviewing their audit log, and keeps the payload lean.
    }
}
