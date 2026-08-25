package com.company.salonbooking.catalog.interfaces.rest;

import com.company.salonbooking.catalog.application.command.ChangeServiceStatusCommand;
import com.company.salonbooking.catalog.application.command.CreateServiceCommand;
import com.company.salonbooking.catalog.application.command.UpdateServiceCommand;
import com.company.salonbooking.catalog.application.usecase.*;
import com.company.salonbooking.catalog.domain.model.ServiceOffering;
import com.company.salonbooking.catalog.interfaces.rest.dto.*;
import com.company.salonbooking.infrastructure.security.AuthenticatedUser;
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
@Tag(name = "Catalog")
public class ServiceController {

    private final CreateServiceUseCase createServiceUseCase;
    private final GetServiceUseCase getServiceUseCase;
    private final ListServicesUseCase listServicesUseCase;
    private final UpdateServiceUseCase updateServiceUseCase;
    private final ChangeServiceStatusUseCase changeServiceStatusUseCase;

    public ServiceController(CreateServiceUseCase createServiceUseCase, GetServiceUseCase getServiceUseCase,
                             ListServicesUseCase listServicesUseCase, UpdateServiceUseCase updateServiceUseCase,
                             ChangeServiceStatusUseCase changeServiceStatusUseCase) {
        this.createServiceUseCase = createServiceUseCase;
        this.getServiceUseCase = getServiceUseCase;
        this.listServicesUseCase = listServicesUseCase;
        this.updateServiceUseCase = updateServiceUseCase;
        this.changeServiceStatusUseCase = changeServiceStatusUseCase;
    }

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

    @GetMapping("/api/v1/businesses/{businessId}/services")
    public ResponseEntity<List<ServiceResponse>> list(@PathVariable UUID businessId,
                                                      @RequestParam(defaultValue = "true") boolean onlyActive,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        List<ServiceResponse> response = listServicesUseCase.execute(businessId, onlyActive, page, size).stream()
                .map(ServiceResponse::from).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/services/{serviceId}")
    public ResponseEntity<ServiceResponse> get(@PathVariable UUID serviceId) {
        return ResponseEntity.ok(ServiceResponse.from(getServiceUseCase.execute(serviceId)));
    }

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

    @PatchMapping("/api/v1/services/{serviceId}/status")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ServiceResponse> changeStatus(@PathVariable UUID serviceId,
                                                        @Valid @RequestBody ChangeServiceStatusRequest request,
                                                        @AuthenticationPrincipal AuthenticatedUser principal) {
        ServiceOffering service = changeServiceStatusUseCase.execute(
                new ChangeServiceStatusCommand(serviceId, principal.userId(), request.active()));

        return ResponseEntity.ok(ServiceResponse.from(service));
    }
}