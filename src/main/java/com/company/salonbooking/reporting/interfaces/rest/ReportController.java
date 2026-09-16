package com.company.salonbooking.reporting.interfaces.rest;

import com.company.salonbooking.infrastructure.security.AuthenticatedUser;
import com.company.salonbooking.reporting.application.command.GenerateReportCommand;
import com.company.salonbooking.reporting.application.usecase.GenerateReportUseCase;
import com.company.salonbooking.reporting.application.usecase.GetReportStatusUseCase;
import com.company.salonbooking.reporting.domain.model.ReportJob;
import com.company.salonbooking.reporting.interfaces.rest.dto.GenerateReportRequest;
import com.company.salonbooking.reporting.interfaces.rest.dto.ReportJobResponse;
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
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports", description = "Report generation and status endpoints")
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

    @Operation(summary = "Generate a report", description = "Initiates asynchronous report generation for the business")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Report generation started successfully",
                content = @Content(schema = @Schema(implementation = ReportJobResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - not the business owner",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Business not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ReportJobResponse> generate(@Valid @RequestBody GenerateReportRequest request,
                                                      @AuthenticationPrincipal AuthenticatedUser principal) {
        ReportJob job = generateReportUseCase.execute(new GenerateReportCommand(
                request.businessId(), principal.userId(), request.type(), request.startDate(), request.endDate()));

        return ResponseEntity.status(HttpStatus.CREATED).body(ReportJobResponse.from(job, objectMapper));
    }

    @Operation(summary = "Get report status", description = "Returns the status and result of a report generation job")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Report retrieved successfully",
                content = @Content(schema = @Schema(implementation = ReportJobResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - not the business owner",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Report not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ReportJobResponse> get(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser principal) {
        ReportJob job = getReportStatusUseCase.execute(id, principal.userId());
        return ResponseEntity.ok(ReportJobResponse.from(job, objectMapper));
    }
}
