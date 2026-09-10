package com.company.salonbooking.audit.infrastructure.persistence;

import com.company.salonbooking.audit.domain.model.AuditEvent;
import com.company.salonbooking.audit.domain.repository.AuditEventRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class AuditEventRepositoryAdapter implements AuditEventRepository {

    private final AuditEventJpaRepository jpaRepository;

    public AuditEventRepositoryAdapter(AuditEventJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public AuditEvent save(AuditEvent event) {
        AuditEventJpaEntity entity = new AuditEventJpaEntity(event.getId(), event.getActorUserId(), event.getBusinessId(),
                event.getAction(), event.getResourceType(), event.getResourceId(), event.getMetadata(),
                event.getIpAddress(), event.getUserAgent(), event.getOccurredAt());
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<AuditEvent> findByBusinessId(UUID businessId, int page, int size) {
        return jpaRepository.findByBusinessIdOrderByOccurredAtDesc(businessId, PageRequest.of(page, size))
                .stream().map(this::toDomain).toList();
    }

    private AuditEvent toDomain(AuditEventJpaEntity e) {
        return AuditEvent.restore(e.getId(), e.getActorUserId(), e.getBusinessId(), e.getAction(), e.getResourceType(),
                e.getResourceId(), e.getMetadata(), e.getIpAddress(), e.getUserAgent(), e.getOccurredAt());
    }
}
