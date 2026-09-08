package com.company.salonbooking.reporting.interfaces.rest.dto;

import com.company.salonbooking.reporting.domain.model.ReportJob;
import com.company.salonbooking.reporting.domain.model.ReportStatus;
import com.company.salonbooking.reporting.domain.model.ReportType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ReportJobResponse(
        UUID id, UUID businessId, ReportType type, LocalDate startDate, LocalDate endDate, ReportStatus status,
        String resultLocation, JsonNode result, String errorMessage,
        Instant createdAt, Instant startedAt, Instant completedAt
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