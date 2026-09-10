package com.company.salonbooking.audit.domain.repository;

import com.company.salonbooking.audit.domain.model.AuditEvent;

import java.util.List;
import java.util.UUID;

public interface AuditEventRepository {

    AuditEvent save(AuditEvent event);

    List<AuditEvent> findByBusinessId(UUID businessId, int page, int size);
}
