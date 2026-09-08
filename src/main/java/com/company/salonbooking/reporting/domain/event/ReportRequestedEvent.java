package com.company.salonbooking.reporting.domain.event;

import com.company.salonbooking.shared.domain.event.DomainEvent;

import java.time.LocalDate;
import java.util.UUID;

public record ReportRequestedEvent(
        UUID reportJobId, UUID businessId, String reportType, LocalDate startDate, LocalDate endDate
) implements DomainEvent {

    @Override
    public String eventType() { return "ReportRequested"; }

    @Override
    public String aggregateType() { return "ReportJob"; }

    @Override
    public UUID aggregateId() { return reportJobId; }
}