package com.company.salonbooking.audit.application.usecase;

import com.company.salonbooking.audit.domain.model.AuditEvent;
import com.company.salonbooking.audit.domain.repository.AuditEventRepository;
import com.company.salonbooking.business.domain.exception.BusinessNotFoundException;
import com.company.salonbooking.business.domain.repository.BusinessRepository;
import com.company.salonbooking.shared.exception.UnauthorizedResourceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Only the OWNER of the business can view its audit trail (Seção 51, 92). */
@Service
public class ListAuditEventsUseCase {

    private static final int MAX_PAGE_SIZE = 100;

    private final AuditEventRepository auditEventRepository;
    private final BusinessRepository businessRepository;

    public ListAuditEventsUseCase(AuditEventRepository auditEventRepository, BusinessRepository businessRepository) {
        this.auditEventRepository = auditEventRepository;
        this.businessRepository = businessRepository;
    }

    @Transactional(readOnly = true)
    public List<AuditEvent> execute(UUID businessId, UUID requesterId, int page, int size) {
        var business = businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessNotFoundException(businessId));

        if (!business.isOwnedBy(requesterId)) {
            throw new UnauthorizedResourceException("You do not own this business.");
        }

        return auditEventRepository.findByBusinessId(businessId, page, Math.min(size, MAX_PAGE_SIZE));
    }
}