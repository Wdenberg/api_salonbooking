package com.company.salonbooking.reporting.interfaces.rest.dto;

import com.company.salonbooking.reporting.domain.model.ReportJob;
import com.company.salonbooking.reporting.domain.model.ReportStatus;
import com.company.salonbooking.reporting.domain.model.ReportType;
import io.swagger.v3.oas.annotations.media.Schema;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ReportJobResponse(
        @Schema(description = "Report job ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
        @Schema(description = "Business ID", example = "550e8400-e29b-41d4-a716-446655440001")
        UUID businessId,
        @Schema(description = "Report type", example = "APPOINTMENTS")
        ReportType type,
        @Schema(description = "Report start date", example = "2026-09-01")
        LocalDate startDate,
        @Schema(description = "Report end date", example = "2026-09-30")
        LocalDate endDate,
        @Schema(description = "Report status", example = "COMPLETED")
        ReportStatus status,
        @Schema(description = "Result storage location", example = "inline")
        String resultLocation,
        @Schema(description = "Report result data (JSON)")
        JsonNode result,
        @Schema(description = "Error message if failed", example = "Failed to generate report")
        String errorMessage,
        @Schema(description = "Creation timestamp", example = "2026-09-16T10:00:00Z")
        Instant createdAt,
        @Schema(description = "Processing start timestamp", example = "2026-09-16T10:00:01Z")
        Instant startedAt,
        @Schema(description = "Completion timestamp", example = "2026-09-16T10:00:05Z")
        Instant completedAt
) {
    public static ReportJobResponse from(ReportJob job, ObjectMapper objectMapper) {
        JsonNode result = null;
        if (job.getResultData() != null) {
            try {
                result = objectMapper.readTree(job.getResultData());
            } catch (Exception ignored) {
                // Malformed stored JSON should never happen in practice; surfaced as null result.
            }
        }

        return new ReportJobResponse(job.getId(), job.getBusinessId(), job.getType(), job.getStartDate(), job.getEndDate(),
                job.getStatus(), job.getResultLocation(), result, job.getErrorMessage(),
                job.getCreatedAt(), job.getStartedAt(), job.getCompletedAt());
    }
}
