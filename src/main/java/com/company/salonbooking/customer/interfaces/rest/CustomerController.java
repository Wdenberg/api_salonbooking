package com.company.salonbooking.customer.interfaces.rest;

import com.company.salonbooking.customer.application.command.UpdateCustomerProfileCommand;
import com.company.salonbooking.customer.application.usecase.GetCustomerProfileUseCase;
import com.company.salonbooking.customer.application.usecase.UpdateCustomerProfileUseCase;
import com.company.salonbooking.customer.interfaces.rest.dto.CustomerProfileResponse;
import com.company.salonbooking.customer.interfaces.rest.dto.UpdateCustomerProfileRequest;
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

@RestController
@RequestMapping("/api/v1/customers/me")
@Tag(name = "Customer", description = "Customer profile management endpoints")
public class CustomerController {

    private final GetCustomerProfileUseCase getUseCase;
    private final UpdateCustomerProfileUseCase updateUseCase;

    public CustomerController(GetCustomerProfileUseCase getUseCase, UpdateCustomerProfileUseCase updateUseCase) {
        this.getUseCase = getUseCase;
        this.updateUseCase = updateUseCase;
    }

    @Operation(summary = "Get customer profile", description = "Returns the profile of the authenticated customer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
                content = @Content(schema = @Schema(implementation = CustomerProfileResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthenticated",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - not a customer",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Profile not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerProfileResponse> get(@AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(CustomerProfileResponse.from(getUseCase.execute(principal.userId())));
    }

    @Operation(summary = "Update customer profile", description = "Updates the profile of the authenticated customer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                content = @Content(schema = @Schema(implementation = CustomerProfileResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthenticated",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - not a customer",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerProfileResponse> update(@RequestBody UpdateCustomerProfileRequest request,
                                                          @AuthenticationPrincipal AuthenticatedUser principal) {
        var profile = updateUseCase.execute(
                new UpdateCustomerProfileCommand(principal.userId(), request.phone(), request.dateOfBirth()));
        return ResponseEntity.ok(CustomerProfileResponse.from(profile));
    }
}
