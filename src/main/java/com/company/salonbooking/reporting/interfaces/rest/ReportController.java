package com.company.salonbooking.reporting.interfaces.rest;

import com.company.salonbooking.infrastructure.security.AuthenticatedUser;
import com.company.salonbooking.reporting.application.command.GenerateReportCommand;
import com.company.salonbooking.reporting.application.usecase.GenerateReportUseCase;
import com.company.salonbooking.reporting.application.usecase.GetReportStatusUseCase;
import com.company.salonbooking.reporting.domain.model.ReportJob;
import com.company.salonbooking.reporting.interfaces.rest.dto.GenerateReportRequest;
import com.company.salonbooking.reporting.interfaces.rest.dto.ReportJobResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports")
public class ReportController {

    private final GenerateReportUseCase generateReportUseCase;
    private final GetReportStatusUseCase getReportStatusUseCase;
    private final ObjectMapper objectMapper;

    public ReportController(GenerateReportUseCase generateReportUseCase, GetReportStatusUseCase getReportStatusUseCase,
                            ObjectMapper objectMapper) {
        this.generateReportUseCase = generateReportUseCase;
        this.getReportStatusUseCase = getReportStatusUseCase;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ReportJobResponse> generate(@Valid @RequestBody GenerateReportRequest request,
                                                      @AuthenticationPrincipal AuthenticatedUser principal) {
        ReportJob job = generateReportUseCase.execute(new GenerateReportCommand(
                request.businessId(), principal.userId(), request.type(), request.startDate(), request.endDate()));

        return ResponseEntity.status(HttpStatus.CREATED).body(ReportJobResponse.from(job, objectMapper));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ReportJobResponse> get(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser principal) {
        ReportJob job = getReportStatusUseCase.execute(id, principal.userId());
        return ResponseEntity.ok(ReportJobResponse.from(job, objectMapper));
    }
}