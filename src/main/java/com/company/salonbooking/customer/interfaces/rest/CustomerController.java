package com.company.salonbooking.customer.interfaces.rest;

import com.company.salonbooking.customer.application.command.UpdateCustomerProfileCommand;
import com.company.salonbooking.customer.application.usecase.GetCustomerProfileUseCase;
import com.company.salonbooking.customer.application.usecase.UpdateCustomerProfileUseCase;
import com.company.salonbooking.customer.interfaces.rest.dto.CustomerProfileResponse;
import com.company.salonbooking.customer.interfaces.rest.dto.UpdateCustomerProfileRequest;
import com.company.salonbooking.infrastructure.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers/me")
@Tag(name = "Customer")
public class CustomerController {

    private final GetCustomerProfileUseCase getUseCase;
    private final UpdateCustomerProfileUseCase updateUseCase;

    public CustomerController(GetCustomerProfileUseCase getUseCase, UpdateCustomerProfileUseCase updateUseCase) {
        this.getUseCase = getUseCase;
        this.updateUseCase = updateUseCase;
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerProfileResponse> get(@AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(CustomerProfileResponse.from(getUseCase.execute(principal.userId())));
    }

    @PutMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerProfileResponse> update(@RequestBody UpdateCustomerProfileRequest request,
                                                          @AuthenticationPrincipal AuthenticatedUser principal) {
        var profile = updateUseCase.execute(
                new UpdateCustomerProfileCommand(principal.userId(), request.phone(), request.dateOfBirth()));
        return ResponseEntity.ok(CustomerProfileResponse.from(profile));
    }
}