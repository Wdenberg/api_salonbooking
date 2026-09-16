package com.company.salonbooking.admin.interfaces.rest;

import com.company.salonbooking.admin.interfaces.rest.dto.AdminAppointmentResponse;
import com.company.salonbooking.scheduling.application.usecase.ListAllAppointmentsUseCase;
import com.company.salonbooking.scheduling.domain.model.Appointment;
import com.company.salonbooking.scheduling.domain.model.AppointmentFilter;
import com.company.salonbooking.scheduling.domain.model.AppointmentStatus;
import com.company.salonbooking.infrastructure.security.AuthenticatedUser;
import com.company.salonbooking.shared.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin - Appointments", description = "Platform admin endpoints for cross-tenant appointment management")
public class AdminAppointmentController {

    private final ListAllAppointmentsUseCase listAllAppointmentsUseCase;

    public AdminAppointmentController(ListAllAppointmentsUseCase listAllAppointmentsUseCase) {
        this.listAllAppointmentsUseCase = listAllAppointmentsUseCase;
    }

    @Operation(summary = "List all appointments (cross-tenant)", description = "Returns a paginated list of all appointments across all businesses. Only accessible by PLATFORM_ADMIN.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Appointments retrieved successfully",
                content = @Content(schema = @Schema(implementation = AdminAppointmentResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthenticated",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - not a platform admin",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/appointments")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<List<AdminAppointmentResponse>> listAll(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) UUID businessId,
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) UUID serviceId,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        var filter = new AppointmentFilter(status, employeeId, serviceId, dateFrom, dateTo);
        List<Appointment> appointments = listAllAppointmentsUseCase.execute(filter, page, size);

        List<AdminAppointmentResponse> response = appointments.stream()
                .map(AdminAppointmentResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }
}