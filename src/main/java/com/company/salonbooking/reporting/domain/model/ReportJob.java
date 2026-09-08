package com.company.salonbooking.reporting.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class ReportJob {

    private final UUID id;
    private final UUID businessId;
    private final UUID requestedBy;
    private final ReportType type;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private ReportStatus status;
    private String resultLocation;
    private String resultData;
    private String errorMessage;
    private final Instant createdAt;
    private Instant startedAt;
    private Instant completedAt;

    private ReportJob(UUID id, UUID businessId, UUID requestedBy, ReportType type, LocalDate startDate, LocalDate endDate,
                      ReportStatus status, String resultLocation, String resultData, String errorMessage,
                      Instant createdAt, Instant startedAt, Instant completedAt) {
        this.id = Objects.requireNonNull(id);
        this.businessId = Objects.requireNonNull(businessId);
        this.requestedBy = Objects.requireNonNull(requestedBy);
        this.type = Objects.requireNonNull(type);
        this.startDate = Objects.requireNonNull(startDate);
        this.endDate = Objects.requireNonNull(endDate);
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must not be after endDate");
        }
        this.status = Objects.requireNonNull(status);
        this.resultLocation = resultLocation;
        this.resultData = resultData;
        this.errorMessage = errorMessage;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    public static ReportJob request(UUID id, UUID businessId, UUID requestedBy, ReportType type,
                                    LocalDate startDate, LocalDate endDate, Instant now) {
        return new ReportJob(id, businessId, requestedBy, type, startDate, endDate, ReportStatus.PENDING,
                null, null, null, now, null, null);
    }

    public static ReportJob restore(UUID id, UUID businessId, UUID requestedBy, ReportType type, LocalDate startDate,
                                    LocalDate endDate, ReportStatus status, String resultLocation, String resultData,
                                    String errorMessage, Instant createdAt, Instant startedAt, Instant completedAt) {
        return new ReportJob(id, businessId, requestedBy, type, startDate, endDate, status, resultLocation,
                resultData, errorMessage, createdAt, startedAt, completedAt);
    }

    public void markProcessing(Instant now) {
        if (status != ReportStatus.PENDING) {
            throw new IllegalStateException("Cannot start processing a report in status " + status);
        }
        this.status = ReportStatus.PROCESSING;
        this.startedAt = now;
    }

    public void markCompleted(String resultLocation, String resultData, Instant now) {
        if (status != ReportStatus.PROCESSING) {
            throw new IllegalStateException("Cannot complete a report in status " + status);
        }
        this.status = ReportStatus.COMPLETED;
        this.resultLocation = resultLocation;
        this.resultData = resultData;
        this.completedAt = now;
    }

    public void markFailed(String errorMessage, Instant now) {
        this.status = ReportStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = now;
    }

    public boolean belongsToBusiness(UUID businessIdToCheck) {
        return businessId.equals(businessIdToCheck);
    }

    public UUID getId() { return id; }
    public UUID getBusinessId() { return businessId; }
    public UUID getRequestedBy() { return requestedBy; }
    public ReportType getType() { return type; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public ReportStatus getStatus() { return status; }
    public String getResultLocation() { return resultLocation; }
    public String getResultData() { return resultData; }
    public String getErrorMessage() { return errorMessage; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReportJob that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}