package com.company.salonbooking.reporting.interfaces.rest.dto;

import com.company.salonbooking.reporting.domain.model.ReportType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record GenerateReportRequest(
        @NotNull UUID businessId,
        @NotNull ReportType type,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {}
