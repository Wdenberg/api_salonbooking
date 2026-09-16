package com.company.salonbooking.reporting.interfaces.rest.dto;

import com.company.salonbooking.reporting.domain.model.ReportType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record GenerateReportRequest(
        @Schema(description = "Business ID", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull UUID businessId,
        @Schema(description = "Report type", example = "APPOINTMENTS")
        @NotNull ReportType type,
        @Schema(description = "Report start date", example = "2026-09-01")
        @NotNull LocalDate startDate,
        @Schema(description = "Report end date", example = "2026-09-30")
        @NotNull LocalDate endDate
) {}
