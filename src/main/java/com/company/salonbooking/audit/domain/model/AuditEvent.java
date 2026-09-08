package com.company.salonbooking.audit.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Persisted record of a sensitive action (Seção 92: "A auditoria não deve depender
 * somente dos logs... Audit Events servem para histórico de ações relevantes").
 * Deliberately separate from application logs — this is a durable, queryable table,
 * not log-aggregator output that can be rotated away.
 */
public final class AuditEvent {

    private final UUID id;
    private final UUID actorUserId;
    private final UUID businessId; // nullable: not every audited action has a business context (e.g. LOGIN)
    private final AuditAction action;
    private final String resourceType;
    private final UUID resourceId;
    private final String metadata;
    private final String ipAddress;
    private final String userAgent;
    private final Instant occurredAt;

    private AuditEvent(UUID id, UUID actorUserId, UUID businessId, AuditAction action, String resourceType,
                       UUID resourceId, String metadata, String ipAddress, String userAgent, Instant occurredAt) {
        this.id = Objects.requireNonNull(id);
        this.actorUserId = Objects.requireNonNull(actorUserId);
        this.businessId = businessId;
        this.action = Objects.requireNonNull(action);
        this.resourceType = Objects.requireNonNull(resourceType);
        this.resourceId = Objects.requireNonNull(resourceId);
        this.metadata = metadata;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.occurredAt = Objects.requireNonNull(occurredAt);
    }

    public static AuditEvent record(UUID id, UUID actorUserId, UUID businessId, AuditAction action, String resourceType,
                                    UUID resourceId, String metadata, String ipAddress, String userAgent, Instant now) {
        return new AuditEvent(id, actorUserId, businessId, action, resourceType, resourceId, metadata,
                ipAddress, userAgent, now);
    }

    public static AuditEvent restore(UUID id, UUID actorUserId, UUID businessId, AuditAction action, String resourceType,
                                     UUID resourceId, String metadata, String ipAddress, String userAgent, Instant occurredAt) {
        return new AuditEvent(id, actorUserId, businessId, action, resourceType, resourceId, metadata,
                ipAddress, userAgent, occurredAt);
    }

    public UUID getId() { return id; }
    public UUID getActorUserId() { return actorUserId; }
    public UUID getBusinessId() { return businessId; }
    public AuditAction getAction() { return action; }
    public String getResourceType() { return resourceType; }
    public UUID getResourceId() { return resourceId; }
    public String getMetadata() { return metadata; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
    public Instant getOccurredAt() { return occurredAt; }
}