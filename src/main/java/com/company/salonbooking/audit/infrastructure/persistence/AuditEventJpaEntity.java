package com.company.salonbooking.audit.infrastructure.persistence;

import com.company.salonbooking.audit.domain.model.AuditAction;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_events")
public class AuditEventJpaEntity {

    @Id
    private UUID id;

    @Column(name = "actor_user_id", nullable = false)
    private UUID actorUserId;

    @Column(name = "business_id")
    private UUID businessId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditAction action;

    @Column(name = "resource_type", nullable = false, length = 50)
    private String resourceType;

    @Column(name = "resource_id", nullable = false)
    private UUID resourceId;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected AuditEventJpaEntity() {
    }

    public AuditEventJpaEntity(UUID id, UUID actorUserId, UUID businessId, AuditAction action, String resourceType,
                               UUID resourceId, String metadata, String ipAddress, String userAgent, Instant occurredAt) {
        this.id = id;
        this.actorUserId = actorUserId;
        this.businessId = businessId;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.metadata = metadata;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.occurredAt = occurredAt;
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
