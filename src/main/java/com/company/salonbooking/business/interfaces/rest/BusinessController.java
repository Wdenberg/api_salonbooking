package com.company.salonbooking.business.interfaces.rest;

import com.company.salonbooking.business.application.command.ChangeBusinessStatusCommand;
import com.company.salonbooking.business.application.command.CreateBusinessCommand;
import com.company.salonbooking.business.application.command.UpdateBusinessCommand;
import com.company.salonbooking.business.application.usecase.ChangeBusinessStatusUseCase;
import com.company.salonbooking.business.application.usecase.CreateBusinessUseCase;
import com.company.salonbooking.business.application.usecase.GetBusinessUseCase;
import com.company.salonbooking.business.application.usecase.UpdateBusinessUseCase;
import com.company.salonbooking.business.domain.model.Address;
import com.company.salonbooking.business.domain.model.Business;
import com.company.salonbooking.business.interfaces.rest.dto.BusinessResponse;
import com.company.salonbooking.business.interfaces.rest.dto.ChangeBusinessStatusRequest;
import com.company.salonbooking.business.interfaces.rest.dto.CreateBusinessRequest;
import com.company.salonbooking.business.interfaces.rest.dto.UpdateBusinessRequest;
import com.company.salonbooking.infrastructure.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/businesses")
@Tag(name = "Business")
public class BusinessController {

    private final CreateBusinessUseCase createBusinessUseCase;
    private final GetBusinessUseCase getBusinessUseCase;
    private final UpdateBusinessUseCase updateBusinessUseCase;
    private final ChangeBusinessStatusUseCase changeBusinessStatusUseCase;

    public BusinessController(CreateBusinessUseCase createBusinessUseCase, GetBusinessUseCase getBusinessUseCase,
                              UpdateBusinessUseCase updateBusinessUseCase,
                              ChangeBusinessStatusUseCase changeBusinessStatusUseCase) {
        this.createBusinessUseCase = createBusinessUseCase;
        this.getBusinessUseCase = getBusinessUseCase;
        this.updateBusinessUseCase = updateBusinessUseCase;
        this.changeBusinessStatusUseCase = changeBusinessStatusUseCase;
    }

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<BusinessResponse> create(@Valid @RequestBody CreateBusinessRequest request,
                                                   @AuthenticationPrincipal AuthenticatedUser principal) {
        Address address = request.address() == null ? Address.empty() : new Address(
                request.address().street(), request.address().number(), request.address().city(),
                request.address().state(), request.address().zipCode(), request.address().country());

        Business business = createBusinessUseCase.execute(new CreateBusinessCommand(
                principal.userId(), request.name(), request.description(), request.phone(), request.email(),
                address, request.timezone()));

        return ResponseEntity.status(HttpStatus.CREATED).body(BusinessResponse.from(business));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusinessResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(BusinessResponse.from(getBusinessUseCase.execute(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<BusinessResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateBusinessRequest request,
                                                   @AuthenticationPrincipal AuthenticatedUser principal) {
        Address address = request.address() == null ? Address.empty() : new Address(
                request.address().street(), request.address().number(), request.address().city(),
                request.address().state(), request.address().zipCode(), request.address().country());

        Business business = updateBusinessUseCase.execute(new UpdateBusinessCommand(
                id, principal.userId(), request.name(), request.description(), request.phone(), request.email(), address));

        return ResponseEntity.ok(BusinessResponse.from(business));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<BusinessResponse> changeStatus(@PathVariable UUID id,
                                                         @Valid @RequestBody ChangeBusinessStatusRequest request,
                                                         @AuthenticationPrincipal AuthenticatedUser principal) {
        Business business = changeBusinessStatusUseCase.execute(
                new ChangeBusinessStatusCommand(id, principal.userId(), request.status()));

        return ResponseEntity.ok(BusinessResponse.from(business));
    }
}