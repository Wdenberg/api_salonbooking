package com.company.salonbooking.business.interfaces.rest;

import com.company.salonbooking.business.application.command.ChangeBusinessStatusCommand;
import com.company.salonbooking.business.application.command.CreateBusinessCommand;
import com.company.salonbooking.business.application.command.DeleteBusinessCommand;
import com.company.salonbooking.business.application.command.UpdateBusinessCommand;
import com.company.salonbooking.business.application.usecase.ChangeBusinessStatusUseCase;
import com.company.salonbooking.business.application.usecase.CreateBusinessUseCase;
import com.company.salonbooking.business.application.usecase.DeleteBusinessUseCase;
import com.company.salonbooking.business.application.usecase.GetBusinessUseCase;
import com.company.salonbooking.business.application.usecase.UpdateBusinessUseCase;
import com.company.salonbooking.business.domain.model.Address;
import com.company.salonbooking.business.domain.model.Business;
import com.company.salonbooking.business.interfaces.rest.dto.BusinessResponse;
import com.company.salonbooking.business.interfaces.rest.dto.ChangeBusinessStatusRequest;
import com.company.salonbooking.business.interfaces.rest.dto.CreateBusinessRequest;
import com.company.salonbooking.business.interfaces.rest.dto.UpdateBusinessRequest;
import com.company.salonbooking.infrastructure.security.AuthenticatedUser;
import com.company.salonbooking.shared.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Business", description = "Business management endpoints")
public class BusinessController {

    private final CreateBusinessUseCase createBusinessUseCase;
    private final GetBusinessUseCase getBusinessUseCase;
    private final UpdateBusinessUseCase updateBusinessUseCase;
    private final ChangeBusinessStatusUseCase changeBusinessStatusUseCase;
    private final DeleteBusinessUseCase deleteBusinessUseCase;

    public BusinessController(CreateBusinessUseCase createBusinessUseCase, GetBusinessUseCase getBusinessUseCase,
                              UpdateBusinessUseCase updateBusinessUseCase,
                              ChangeBusinessStatusUseCase changeBusinessStatusUseCase,
                              DeleteBusinessUseCase deleteBusinessUseCase) {
        this.createBusinessUseCase = createBusinessUseCase;
        this.getBusinessUseCase = getBusinessUseCase;
        this.updateBusinessUseCase = updateBusinessUseCase;
        this.changeBusinessStatusUseCase = changeBusinessStatusUseCase;
        this.deleteBusinessUseCase = deleteBusinessUseCase;
    }

    @Operation(summary = "Create a new business", description = "Creates a new business for the authenticated owner")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Business created successfully",
                content = @Content(schema = @Schema(implementation = BusinessResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthenticated",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - not an owner",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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

    @Operation(summary = "Get business by ID", description = "Returns the business details for the given ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Business retrieved successfully",
                content = @Content(schema = @Schema(implementation = BusinessResponse.class))),
        @ApiResponse(responseCode = "404", description = "Business not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<BusinessResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(BusinessResponse.from(getBusinessUseCase.execute(id)));
    }

@Operation(summary = "Update a business", description = "Updates the business details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Business updated successfully",
                content = @Content(schema = @Schema(implementation = BusinessResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthenticated",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - not the business owner",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Business not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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

@Operation(summary = "Change business status", description = "Changes the business status (ACTIVE/INACTIVE)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Business status changed successfully",
                content = @Content(schema = @Schema(implementation = BusinessResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthenticated",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - not the business owner",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Business not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<BusinessResponse> changeStatus(@PathVariable UUID id,
                                                          @Valid @RequestBody ChangeBusinessStatusRequest request,
                                                          @AuthenticationPrincipal AuthenticatedUser principal) {
        Business business = changeBusinessStatusUseCase.execute(
                new ChangeBusinessStatusCommand(id, principal.userId(), request.status()));

        return ResponseEntity.ok(BusinessResponse.from(business));
    }

@Operation(summary = "Delete a business", description = "Soft deletes a business by changing its status to INACTIVE")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Business deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthenticated",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - not the business owner",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Business not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Business has active appointments or employees",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                        @AuthenticationPrincipal AuthenticatedUser principal) {
        deleteBusinessUseCase.execute(new DeleteBusinessCommand(id, principal.userId()));
        return ResponseEntity.noContent().build();
    }
}
