package com.company.salonbooking.scheduling.interfaces.rest;

import com.company.salonbooking.infrastructure.security.AuthenticatedUser;
import com.company.salonbooking.scheduling.application.command.*;
import com.company.salonbooking.scheduling.application.usecase.*;
import com.company.salonbooking.scheduling.domain.model.Appointment;
import com.company.salonbooking.scheduling.domain.model.AppointmentStatus;
import com.company.salonbooking.scheduling.domain.repository.AppointmentFilter;
import com.company.salonbooking.scheduling.interfaces.rest.dto.AppointmentResponse;
import com.company.salonbooking.scheduling.interfaces.rest.dto.CreateAppointmentRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Appointments")
public class AppointmentController {

    private final CreateAppointmentUseCase createAppointmentUseCase;
    private final GetAppointmentUseCase getAppointmentUseCase;
    private final ConfirmAppointmentUseCase confirmAppointmentUseCase;
    private final CancelAppointmentUseCase cancelAppointmentUseCase;
    private final CompleteAppointmentUseCase completeAppointmentUseCase;
    private final ListCustomerAppointmentsUseCase listCustomerAppointmentsUseCase;
    private final ListBusinessAppointmentsUseCase listBusinessAppointmentsUseCase;

    public AppointmentController(CreateAppointmentUseCase createAppointmentUseCase, GetAppointmentUseCase getAppointmentUseCase,
                                 ConfirmAppointmentUseCase confirmAppointmentUseCase, CancelAppointmentUseCase cancelAppointmentUseCase,
                                 CompleteAppointmentUseCase completeAppointmentUseCase,
                                 ListCustomerAppointmentsUseCase listCustomerAppointmentsUseCase,
                                 ListBusinessAppointmentsUseCase listBusinessAppointmentsUseCase) {
        this.createAppointmentUseCase = createAppointmentUseCase;
        this.getAppointmentUseCase = getAppointmentUseCase;
        this.confirmAppointmentUseCase = confirmAppointmentUseCase;
        this.cancelAppointmentUseCase = cancelAppointmentUseCase;
        this.completeAppointmentUseCase = completeAppointmentUseCase;
        this.listCustomerAppointmentsUseCase = listCustomerAppointmentsUseCase;
        this.listBusinessAppointmentsUseCase = listBusinessAppointmentsUseCase;
    }

    @PostMapping("/api/v1/appointments")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody CreateAppointmentRequest request,
                                                      @AuthenticationPrincipal AuthenticatedUser principal) {
        // customerId always comes from the authenticated principal, never from the body (Seção 124).
        Appointment appointment = createAppointmentUseCase.execute(new CreateAppointmentCommand(
                principal.userId(), request.businessId(), request.employeeId(), request.serviceId(),
                request.startAt(), request.notes()));

        return ResponseEntity.status(HttpStatus.CREATED).body(AppointmentResponse.from(appointment));
    }

    @GetMapping("/api/v1/appointments/{id}")
    public ResponseEntity<AppointmentResponse> get(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(AppointmentResponse.from(getAppointmentUseCase.execute(id, principal.userId())));
    }

    @PatchMapping("/api/v1/appointments/{id}/confirm")
    @PreAuthorize("hasAnyRole('OWNER','EMPLOYEE')")
    public ResponseEntity<AppointmentResponse> confirm(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser principal) {
        Appointment appointment = confirmAppointmentUseCase.execute(new ConfirmAppointmentCommand(id, principal.userId()));
        return ResponseEntity.ok(AppointmentResponse.from(appointment));
    }

    @PatchMapping("/api/v1/appointments/{id}/cancel")
    public ResponseEntity<AppointmentResponse> cancel(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser principal) {
        Appointment appointment = cancelAppointmentUseCase.execute(new CancelAppointmentCommand(id, principal.userId()));
        return ResponseEntity.ok(AppointmentResponse.from(appointment));
    }

    @PatchMapping("/api/v1/appointments/{id}/complete")
    @PreAuthorize("hasAnyRole('OWNER','EMPLOYEE')")
    public ResponseEntity<AppointmentResponse> complete(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser principal) {
        Appointment appointment = completeAppointmentUseCase.execute(new CompleteAppointmentCommand(id, principal.userId()));
        return ResponseEntity.ok(AppointmentResponse.from(appointment));
    }

    @GetMapping("/api/v1/customers/me/appointments")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<AppointmentResponse>> myAppointments(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) UUID serviceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        var filter = new AppointmentFilter(status, employeeId, serviceId, dateFrom, dateTo);
        List<AppointmentResponse> response = listCustomerAppointmentsUseCase
                .execute(principal.userId(), filter, page, size).stream()
                .map(AppointmentResponse::from).toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/businesses/{businessId}/appointments")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<AppointmentResponse>> businessAppointments(
            @PathVariable UUID businessId,
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) UUID serviceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        var filter = new AppointmentFilter(status, employeeId, serviceId, dateFrom, dateTo);
        List<AppointmentResponse> response = listBusinessAppointmentsUseCase
                .execute(businessId, principal.userId(), filter, page, size).stream()
                .map(AppointmentResponse::from).toList();

        return ResponseEntity.ok(response);
    }
}