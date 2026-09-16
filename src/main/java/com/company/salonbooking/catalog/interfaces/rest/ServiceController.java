package com.company.salonbooking.catalog.interfaces.rest;

import com.company.salonbooking.catalog.application.command.ChangeServiceStatusCommand;
import com.company.salonbooking.catalog.application.command.CreateServiceCommand;
import com.company.salonbooking.catalog.application.command.DeleteServiceCommand;
import com.company.salonbooking.catalog.application.command.UpdateServiceCommand;
import com.company.salonbooking.catalog.application.usecase.*;
import com.company.salonbooking.catalog.domain.model.ServiceOffering;
import com.company.salonbooking.catalog.interfaces.rest.dto.*;
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

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Catalog", description = "Service catalog management endpoints")
public class ServiceController {

    private final CreateServiceUseCase createServiceUseCase;
    private final GetServiceUseCase getServiceUseCase;
    private final ListServicesUseCase listServicesUseCase;
    private final UpdateServiceUseCase updateServiceUseCase;
    private final ChangeServiceStatusUseCase changeServiceStatusUseCase;
    private final DeleteServiceUseCase deleteServiceUseCase;

    public ServiceController(CreateServiceUseCase createServiceUseCase, GetServiceUseCase getServiceUseCase,
                             ListServicesUseCase listServicesUseCase, UpdateServiceUseCase updateServiceUseCase,
                             ChangeServiceStatusUseCase changeServiceStatusUseCase,
                             DeleteServiceUseCase deleteServiceUseCase) {
        this.createServiceUseCase = createServiceUseCase;
        this.getServiceUseCase = getServiceUseCase;
        this.listServicesUseCase = listServicesUseCase;
        this.updateServiceUseCase = updateServiceUseCase;
        this.changeServiceStatusUseCase = changeServiceStatusUseCase;
        this.deleteServiceUseCase = deleteServiceUseCase;
    }

    @Operation(summary = "Create a new service", description = "Creates a new service offering for the business")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Service created successfully",
                content = @Content(schema = @Schema(implementation = ServiceResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - not the business owner",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Business not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/api/v1/businesses/{businessId}/services")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ServiceResponse> create(@PathVariable UUID businessId,
                                                  @Valid @RequestBody CreateServiceRequest request,
                                                  @AuthenticationPrincipal AuthenticatedUser principal) {
        ServiceOffering service = createServiceUseCase.execute(new CreateServiceCommand(
                principal.userId(), businessId, request.name(), request.description(),
                request.priceAmount(), request.priceCurrency(), request.durationMinutes()));

        return ResponseEntity.status(HttpStatus.CREATED).body(ServiceResponse.from(service));
    }

    @Operation(summary = "List services for a business", description = "Returns a paginated list of services for the given business")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Services retrieved successfully",
                content = @Content(schema = @Schema(implementation = ServiceResponse.class))),
        @ApiResponse(responseCode = "404", description = "Business not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/api/v1/businesses/{businessId}/services")
    public ResponseEntity<List<ServiceResponse>> list(@PathVariable UUID businessId,
                                                      @RequestParam(defaultValue = "true") boolean onlyActive,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        List<ServiceResponse> response = listServicesUseCase.execute(businessId, onlyActive, page, size).stream()
                .map(ServiceResponse::from).toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get a service by ID", description = "Returns the service details for the given ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Service retrieved successfully",
                content = @Content(schema = @Schema(implementation = ServiceResponse.class))),
        @ApiResponse(responseCode = "404", description = "Service not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/api/v1/services/{serviceId}")
    public ResponseEntity<ServiceResponse> get(@PathVariable UUID serviceId) {
        return ResponseEntity.ok(ServiceResponse.from(getServiceUseCase.execute(serviceId)));
    }

    @Operation(summary = "Update a service", description = "Updates the service details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Service updated successfully",
                content = @Content(schema = @Schema(implementation = ServiceResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - not the business owner",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Service not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/api/v1/services/{serviceId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ServiceResponse> update(@PathVariable UUID serviceId,
                                                  @Valid @RequestBody UpdateServiceRequest request,
                                                  @AuthenticationPrincipal AuthenticatedUser principal) {
        ServiceOffering service = updateServiceUseCase.execute(new UpdateServiceCommand(
                serviceId, principal.userId(), request.name(), request.description(),
                request.priceAmount(), request.priceCurrency(), request.durationMinutes()));

        return ResponseEntity.ok(ServiceResponse.from(service));
    }

    @Operation(summary = "Change service status", description = "Activates or deactivates a service")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Service status changed successfully",
                content = @Content(schema = @Schema(implementation = ServiceResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - not the business owner",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Service not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/api/v1/services/{serviceId}/status")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ServiceResponse> changeStatus(@PathVariable UUID serviceId,
                                                        @Valid @RequestBody ChangeServiceStatusRequest request,
                                                        @AuthenticationPrincipal AuthenticatedUser principal) {
        ServiceOffering service = changeServiceStatusUseCase.execute(
                new ChangeServiceStatusCommand(serviceId, principal.userId(), request.active()));

        return ResponseEntity.ok(ServiceResponse.from(service));
    }

    @Operation(summary = "Delete a service", description = "Soft deletes a service by changing its status to inactive")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Service deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - not the business owner",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Service not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Service has active appointments",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/api/v1/services/{serviceId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> delete(@PathVariable UUID serviceId,
                                       @AuthenticationPrincipal AuthenticatedUser principal) {
        deleteServiceUseCase.execute(new DeleteServiceCommand(serviceId, principal.userId()));
        return ResponseEntity.noContent().build();
    }
}
