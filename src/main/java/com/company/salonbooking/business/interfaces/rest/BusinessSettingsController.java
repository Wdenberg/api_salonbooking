package com.company.salonbooking.business.interfaces.rest;

import com.company.salonbooking.business.application.command.UpdateBusinessSettingsCommand;
import com.company.salonbooking.business.application.usecase.GetBusinessSettingsUseCase;
import com.company.salonbooking.business.application.usecase.UpdateBusinessSettingsUseCase;
import com.company.salonbooking.business.interfaces.rest.dto.BusinessSettingsRequest;
import com.company.salonbooking.business.interfaces.rest.dto.BusinessSettingsResponse;
import com.company.salonbooking.infrastructure.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/businesses/{businessId}/settings")
@Tag(name = "Business Settings")
public class BusinessSettingsController {

    private final GetBusinessSettingsUseCase getUseCase;
    private final UpdateBusinessSettingsUseCase updateUseCase;

    public BusinessSettingsController(GetBusinessSettingsUseCase getUseCase, UpdateBusinessSettingsUseCase updateUseCase) {
        this.getUseCase = getUseCase;
        this.updateUseCase = updateUseCase;
    }

    @GetMapping
    public ResponseEntity<BusinessSettingsResponse> get(@PathVariable UUID businessId) {
        return ResponseEntity.ok(BusinessSettingsResponse.from(getUseCase.execute(businessId)));
    }

    @PutMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<BusinessSettingsResponse> update(@PathVariable UUID businessId,
                                                           @Valid @RequestBody BusinessSettingsRequest request,
                                                           @AuthenticationPrincipal AuthenticatedUser principal) {
        var settings = updateUseCase.execute(new UpdateBusinessSettingsCommand(
                businessId, principal.userId(), request.minimumAdvanceMinutes(), request.maximumAdvanceDays(),
                request.cancellationMinimumMinutes(), request.slotIntervalMinutes()));

        return ResponseEntity.ok(BusinessSettingsResponse.from(settings));
    }
}