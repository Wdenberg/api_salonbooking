package com.company.salonbooking.reporting.application.command;

import com.company.salonbooking.reporting.domain.model.ReportType;

import java.time.LocalDate;
import java.util.UUID;

public record GenerateReportCommand(UUID businessId, UUID requesterId, ReportType type, LocalDate startDate, LocalDate endDate) {}
