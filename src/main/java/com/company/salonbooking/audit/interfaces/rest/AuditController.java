package com.company.salonbooking.audit.interfaces.rest;

import com.company.salonbooking.audit.application.usecase.ListAuditEventsUseCase;
import com.company.salonbooking.audit.interfaces.rest.dto.AuditEventResponse;
import com.company.salonbooking.infrastructure.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/businesses/{businessId}/audit-events")
@Tag(name = "Audit")
public class AuditController {

    private final ListAuditEventsUseCase listAuditEventsUseCase;

    public AuditController(ListAuditEventsUseCase listAuditEventsUseCase) {
        this.listAuditEventsUseCase = listAuditEventsUseCase;
    }

    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<AuditEventResponse>> list(@PathVariable UUID businessId,
                                                         @AuthenticationPrincipal AuthenticatedUser principal,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size) {
        List<AuditEventResponse> response = listAuditEventsUseCase.execute(businessId, principal.userId(), page, size)
                .stream().map(AuditEventResponse::from).toList();
        return ResponseEntity.ok(response);
    }
}