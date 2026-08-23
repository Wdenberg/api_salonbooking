package com.company.salonbooking.employee.interfaces.rest;

import com.company.salonbooking.employee.application.command.ChangeEmployeeStatusCommand;
import com.company.salonbooking.employee.application.command.CreateEmployeeCommand;
import com.company.salonbooking.employee.application.command.UpdateEmployeeCommand;
import com.company.salonbooking.employee.application.usecase.*;
import com.company.salonbooking.employee.domain.model.Employee;
import com.company.salonbooking.employee.interfaces.rest.dto.*;
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
@Tag(name = "Employees")
public class EmployeeController {

    private final CreateEmployeeUseCase createEmployeeUseCase;
    private final GetEmployeeUseCase getEmployeeUseCase;
    private final ListEmployeesUseCase listEmployeesUseCase;
    private final UpdateEmployeeUseCase updateEmployeeUseCase;
    private final ChangeEmployeeStatusUseCase changeEmployeeStatusUseCase;

    public EmployeeController(CreateEmployeeUseCase createEmployeeUseCase, GetEmployeeUseCase getEmployeeUseCase,
                              ListEmployeesUseCase listEmployeesUseCase, UpdateEmployeeUseCase updateEmployeeUseCase,
                              ChangeEmployeeStatusUseCase changeEmployeeStatusUseCase) {
        this.createEmployeeUseCase = createEmployeeUseCase;
        this.getEmployeeUseCase = getEmployeeUseCase;
        this.listEmployeesUseCase = listEmployeesUseCase;
        this.updateEmployeeUseCase = updateEmployeeUseCase;
        this.changeEmployeeStatusUseCase = changeEmployeeStatusUseCase;
    }

    @PostMapping("/api/v1/businesses/{businessId}/employees")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<EmployeeResponse> create(@PathVariable UUID businessId,
                                                   @Valid @RequestBody CreateEmployeeRequest request,
                                                   @AuthenticationPrincipal AuthenticatedUser principal) {
        Employee employee = createEmployeeUseCase.execute(new CreateEmployeeCommand(
                principal.userId(), businessId, request.name(), request.email(), request.password(), request.specialty()));

        return ResponseEntity.status(HttpStatus.CREATED).body(EmployeeResponse.from(employee));
    }

    @GetMapping("/api/v1/businesses/{businessId}/employees")
    public ResponseEntity<List<EmployeeResponse>> list(@PathVariable UUID businessId,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size) {
        List<EmployeeResponse> response = listEmployeesUseCase.execute(businessId, page, size).stream()
                .map(EmployeeResponse::from).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/employees/{employeeId}")
    public ResponseEntity<EmployeeResponse> get(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(EmployeeResponse.from(getEmployeeUseCase.execute(employeeId)));
    }

    @PutMapping("/api/v1/employees/{employeeId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<EmployeeResponse> update(@PathVariable UUID employeeId,
                                                   @Valid @RequestBody UpdateEmployeeRequest request,
                                                   @AuthenticationPrincipal AuthenticatedUser principal) {
        Employee employee = updateEmployeeUseCase.execute(
                new UpdateEmployeeCommand(employeeId, principal.userId(), request.specialty()));
        return ResponseEntity.ok(EmployeeResponse.from(employee));
    }

    @PatchMapping("/api/v1/employees/{employeeId}/status")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<EmployeeResponse> changeStatus(@PathVariable UUID employeeId,
                                                         @Valid @RequestBody ChangeEmployeeStatusRequest request,
                                                         @AuthenticationPrincipal AuthenticatedUser principal) {
        Employee employee = changeEmployeeStatusUseCase.execute(
                new ChangeEmployeeStatusCommand(employeeId, principal.userId(), request.status()));
        return ResponseEntity.ok(EmployeeResponse.from(employee));
    }
}