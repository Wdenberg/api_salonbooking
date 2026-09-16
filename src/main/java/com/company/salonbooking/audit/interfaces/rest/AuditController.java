package com.company.salonbooking.audit.interfaces.rest;

import com.company.salonbooking.audit.application.usecase.ListAuditEventsUseCase;
import com.company.salonbooking.audit.interfaces.rest.dto.AuditEventResponse;
import com.company.salonbooking.infrastructure.security.AuthenticatedUser;
import com.company.salonbooking.shared.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/businesses/{businessId}/audit-events")
@Tag(name = "Audit", description = "Audit trail endpoints")
public class AuditController {

    private final ListAuditEventsUseCase listAuditEventsUseCase;

    public AuditController(ListAuditEventsUseCase listAuditEventsUseCase) {
        this.listAuditEventsUseCase = listAuditEventsUseCase;
    }

    @Operation(summary = "List audit events for a business", description = "Returns a paginated list of audit events for the business owned by the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Audit events retrieved successfully",
                content = @Content(schema = @Schema(implementation = AuditEventResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - not the business owner",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Business not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
