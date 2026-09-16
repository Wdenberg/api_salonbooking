package com.company.salonbooking.employee.interfaces.rest;

import com.company.salonbooking.employee.application.command.ChangeEmployeeStatusCommand;
import com.company.salonbooking.employee.application.command.CreateEmployeeCommand;
import com.company.salonbooking.employee.application.command.DeleteEmployeeCommand;
import com.company.salonbooking.employee.application.command.UpdateEmployeeCommand;
import com.company.salonbooking.employee.application.usecase.*;
import com.company.salonbooking.employee.domain.model.Employee;
import com.company.salonbooking.employee.interfaces.rest.dto.*;
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
@Tag(name = "Employees", description = "Employee management endpoints")
public class EmployeeController {

    private final CreateEmployeeUseCase createEmployeeUseCase;
    private final GetEmployeeUseCase getEmployeeUseCase;
    private final ListEmployeesUseCase listEmployeesUseCase;
    private final UpdateEmployeeUseCase updateEmployeeUseCase;
    private final ChangeEmployeeStatusUseCase changeEmployeeStatusUseCase;
    private final DeleteEmployeeUseCase deleteEmployeeUseCase;

    public EmployeeController(CreateEmployeeUseCase createEmployeeUseCase, GetEmployeeUseCase getEmployeeUseCase,
                              ListEmployeesUseCase listEmployeesUseCase, UpdateEmployeeUseCase updateEmployeeUseCase,
                              ChangeEmployeeStatusUseCase changeEmployeeStatusUseCase,
                              DeleteEmployeeUseCase deleteEmployeeUseCase) {
        this.createEmployeeUseCase = createEmployeeUseCase;
        this.getEmployeeUseCase = getEmployeeUseCase;
        this.listEmployeesUseCase = listEmployeesUseCase;
        this.updateEmployeeUseCase = updateEmployeeUseCase;
        this.changeEmployeeStatusUseCase = changeEmployeeStatusUseCase;
        this.deleteEmployeeUseCase = deleteEmployeeUseCase;
    }

@Operation(summary = "Create a new employee", description = "Creates a new employee for the business")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Employee created successfully",
                content = @Content(schema = @Schema(implementation = EmployeeResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - not the business owner",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Business not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Email already exists",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/api/v1/businesses/{businessId}/employees")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<EmployeeResponse> create(@PathVariable UUID businessId,
                                                    @Valid @RequestBody CreateEmployeeRequest request,
                                                    @AuthenticationPrincipal AuthenticatedUser principal) {
        Employee employee = createEmployeeUseCase.execute(new CreateEmployeeCommand(
                principal.userId(), businessId, request.name(), request.email(), request.password(), request.specialty()));

        return ResponseEntity.status(HttpStatus.CREATED).body(EmployeeResponse.from(employee));
    }

@Operation(summary = "List employees for a business", description = "Returns a paginated list of employees for the given business")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Employees retrieved successfully",
                content = @Content(schema = @Schema(implementation = EmployeeResponse.class))),
        @ApiResponse(responseCode = "404", description = "Business not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/api/v1/businesses/{businessId}/employees")
    public ResponseEntity<List<EmployeeResponse>> list(@PathVariable UUID businessId,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        List<EmployeeResponse> response = listEmployeesUseCase.execute(businessId, page, size).stream()
                .map(EmployeeResponse::from).toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get employee by ID", description = "Returns the employee details for the given ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Employee retrieved successfully",
                content = @Content(schema = @Schema(implementation = EmployeeResponse.class))),
        @ApiResponse(responseCode = "404", description = "Employee not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/api/v1/employees/{employeeId}")
    public ResponseEntity<EmployeeResponse> get(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(EmployeeResponse.from(getEmployeeUseCase.execute(employeeId)));
    }

@Operation(summary = "Update an employee", description = "Updates the employee specialty")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Employee updated successfully",
                content = @Content(schema = @Schema(implementation = EmployeeResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - not the business owner",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Employee not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/api/v1/employees/{employeeId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<EmployeeResponse> update(@PathVariable UUID employeeId,
                                                    @Valid @RequestBody UpdateEmployeeRequest request,
                                                    @AuthenticationPrincipal AuthenticatedUser principal) {
        Employee employee = updateEmployeeUseCase.execute(
                new UpdateEmployeeCommand(employeeId, principal.userId(), request.specialty()));
        return ResponseEntity.ok(EmployeeResponse.from(employee));
    }

@Operation(summary = "Change employee status", description = "Changes the employee status (ACTIVE/INACTIVE)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Employee status changed successfully",
                content = @Content(schema = @Schema(implementation = EmployeeResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - not the business owner",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Employee not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/api/v1/employees/{employeeId}/status")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<EmployeeResponse> changeStatus(@PathVariable UUID employeeId,
                                                          @Valid @RequestBody ChangeEmployeeStatusRequest request,
                                                          @AuthenticationPrincipal AuthenticatedUser principal) {
        Employee employee = changeEmployeeStatusUseCase.execute(
                new ChangeEmployeeStatusCommand(employeeId, principal.userId(), request.status()));
        return ResponseEntity.ok(EmployeeResponse.from(employee));
    }

@Operation(summary = "Delete an employee", description = "Soft deletes an employee by changing its status to INACTIVE")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Employee deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - not the business owner",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Employee not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Employee has active appointments",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/api/v1/employees/{employeeId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> delete(@PathVariable UUID employeeId,
                                        @AuthenticationPrincipal AuthenticatedUser principal) {
        deleteEmployeeUseCase.execute(new DeleteEmployeeCommand(employeeId, principal.userId()));
        return ResponseEntity.noContent().build();
    }
}
