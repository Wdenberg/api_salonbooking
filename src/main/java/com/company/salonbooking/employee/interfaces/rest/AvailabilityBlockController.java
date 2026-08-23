package com.company.salonbooking.employee.interfaces.rest;

import com.company.salonbooking.employee.application.command.CreateAvailabilityBlockCommand;
import com.company.salonbooking.employee.application.command.DeleteAvailabilityBlockCommand;
import com.company.salonbooking.employee.application.usecase.CreateAvailabilityBlockUseCase;
import com.company.salonbooking.employee.application.usecase.DeleteAvailabilityBlockUseCase;
import com.company.salonbooking.employee.domain.model.AvailabilityBlock;
import com.company.salonbooking.employee.interfaces.rest.dto.AvailabilityBlockResponse;
import com.company.salonbooking.employee.interfaces.rest.dto.CreateAvailabilityBlockRequest;
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
@Tag(name = "Availability Blocks")
public class AvailabilityBlockController {

    private final CreateAvailabilityBlockUseCase createUseCase;
    private final DeleteAvailabilityBlockUseCase deleteUseCase;

    public AvailabilityBlockController(CreateAvailabilityBlockUseCase createUseCase, DeleteAvailabilityBlockUseCase deleteUseCase) {
        this.createUseCase = createUseCase;
        this.deleteUseCase = deleteUseCase;
    }

    @PostMapping("/api/v1/employees/{employeeId}/availability-blocks")
    @PreAuthorize("hasAnyRole('OWNER','EMPLOYEE')")
    public ResponseEntity<AvailabilityBlockResponse> create(@PathVariable UUID employeeId,
                                                            @Valid @RequestBody CreateAvailabilityBlockRequest request,
                                                            @AuthenticationPrincipal AuthenticatedUser principal) {
        AvailabilityBlock block = createUseCase.execute(new CreateAvailabilityBlockCommand(
                employeeId, principal.userId(), request.startAt(), request.endAt(), request.reason()));

        return ResponseEntity.status(HttpStatus.CREATED).body(AvailabilityBlockResponse.from(block));
    }

    @DeleteMapping("/api/v1/availability-blocks/{id}")
    @PreAuthorize("hasAnyRole('OWNER','EMPLOYEE')")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser principal) {
        deleteUseCase.execute(new DeleteAvailabilityBlockCommand(id, principal.userId()));
        return ResponseEntity.noContent().build();
    }
}