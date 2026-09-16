package com.company.salonbooking.employee.interfaces.rest;

import com.company.salonbooking.employee.application.command.CreateAvailabilityBlockCommand;
import com.company.salonbooking.employee.application.command.DeleteAvailabilityBlockCommand;
import com.company.salonbooking.employee.application.usecase.CreateAvailabilityBlockUseCase;
import com.company.salonbooking.employee.application.usecase.DeleteAvailabilityBlockUseCase;
import com.company.salonbooking.employee.application.usecase.ListAvailabilityBlocksUseCase;
import com.company.salonbooking.employee.domain.model.AvailabilityBlock;
import com.company.salonbooking.employee.interfaces.rest.dto.AvailabilityBlockResponse;
import com.company.salonbooking.employee.interfaces.rest.dto.CreateAvailabilityBlockRequest;
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
@Tag(name = "Availability Blocks", description = "Employee availability block management endpoints")
public class AvailabilityBlockController {

    private final CreateAvailabilityBlockUseCase createUseCase;
    private final DeleteAvailabilityBlockUseCase deleteUseCase;
    private final ListAvailabilityBlocksUseCase listUseCase;

    public AvailabilityBlockController(CreateAvailabilityBlockUseCase createUseCase,
                                       DeleteAvailabilityBlockUseCase deleteUseCase,
                                       ListAvailabilityBlocksUseCase listUseCase) {
        this.createUseCase = createUseCase;
        this.deleteUseCase = deleteUseCase;
        this.listUseCase = listUseCase;
    }

    @Operation(summary = "Create an availability block", description = "Creates a time block during which the employee is not available for appointments")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Availability block created successfully",
                content = @Content(schema = @Schema(implementation = AvailabilityBlockResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - not the business owner or employee",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Employee not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Overlaps with existing block",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/api/v1/employees/{employeeId}/availability-blocks")
    @PreAuthorize("hasAnyRole('OWNER','EMPLOYEE')")
    public ResponseEntity<AvailabilityBlockResponse> create(@PathVariable UUID employeeId,
                                                             @Valid @RequestBody CreateAvailabilityBlockRequest request,
                                                             @AuthenticationPrincipal AuthenticatedUser principal) {
        AvailabilityBlock block = createUseCase.execute(new CreateAvailabilityBlockCommand(
                employeeId, principal.userId(), request.startAt(), request.endAt(), request.reason()));

        return ResponseEntity.status(HttpStatus.CREATED).body(AvailabilityBlockResponse.from(block));
    }

    @Operation(summary = "List availability blocks for an employee", description = "Returns all availability blocks for the given employee")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Availability blocks retrieved successfully",
                content = @Content(schema = @Schema(implementation = AvailabilityBlockResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - not the business owner or employee",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Employee not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/api/v1/employees/{employeeId}/availability-blocks")
    @PreAuthorize("hasAnyRole('OWNER','EMPLOYEE')")
    public ResponseEntity<List<AvailabilityBlockResponse>> list(@PathVariable UUID employeeId,
                                                                 @AuthenticationPrincipal AuthenticatedUser principal) {
        List<AvailabilityBlockResponse> response = listUseCase.execute(employeeId).stream()
                .map(AvailabilityBlockResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete an availability block", description = "Removes an availability block by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Availability block deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - not the business owner or employee",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Availability block not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/api/v1/availability-blocks/{id}")
    @PreAuthorize("hasAnyRole('OWNER','EMPLOYEE')")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser principal) {
        deleteUseCase.execute(new DeleteAvailabilityBlockCommand(id, principal.userId()));
        return ResponseEntity.noContent().build();
    }
}
