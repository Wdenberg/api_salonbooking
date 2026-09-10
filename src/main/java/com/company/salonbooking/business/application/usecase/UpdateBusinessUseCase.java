package com.company.salonbooking.business.application.usecase;

import com.company.salonbooking.audit.domain.model.AuditAction;
import com.company.salonbooking.business.application.command.UpdateBusinessCommand;
import com.company.salonbooking.business.domain.exception.BusinessNotFoundException;
import com.company.salonbooking.business.domain.model.Business;
import com.company.salonbooking.business.domain.repository.BusinessRepository;
import com.company.salonbooking.shared.application.port.AuditRecorder;
import com.company.salonbooking.shared.exception.UnauthorizedResourceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class UpdateBusinessUseCase {

    private final BusinessRepository businessRepository;
    private final AuditRecorder auditRecorder;
    private final Clock clock;

    public UpdateBusinessUseCase(BusinessRepository businessRepository, AuditRecorder auditRecorder, Clock clock) {
        this.businessRepository = businessRepository;
        this.auditRecorder = auditRecorder;
        this.clock = clock;
    }

    @Transactional
    public Business execute(UpdateBusinessCommand command) {
        Business business = businessRepository.findById(command.businessId())
                .orElseThrow(() -> new BusinessNotFoundException(command.businessId()));

        // Ownership boundary (Seção 51): only the owner of THIS business may modify it.
        if (!business.isOwnedBy(command.requesterId())) {
            throw new UnauthorizedResourceException("You do not own this business.");
        }

        business.update(command.name(), command.description(), command.phone(), command.email(),
                command.address(), Instant.now(clock));

        Business saved =  businessRepository.save(business);
        auditRecorder.record(command.requesterId(), saved.getId(), AuditAction.UPDATE_BUSINESS, "Business", saved.getId(), null);
        return  saved;
    }
}
