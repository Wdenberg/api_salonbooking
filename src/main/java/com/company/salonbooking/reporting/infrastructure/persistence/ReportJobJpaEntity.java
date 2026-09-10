package com.company.salonbooking.reporting.infrastructure.persistence;

import com.company.salonbooking.reporting.domain.model.ReportStatus;
import com.company.salonbooking.reporting.domain.model.ReportType;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "report_jobs")
public class ReportJobJpaEntity {

    @Id
    private UUID id;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Column(name = "requested_by", nullable = false)
    private UUID requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReportType type;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status;

    @Column(name = "result_location", length = 500)
    private String resultLocation;

    @Column(name = "result_data", columnDefinition = "TEXT")
    private String resultData;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected ReportJobJpaEntity() {
    }

    public ReportJobJpaEntity(UUID id, UUID businessId, UUID requestedBy, ReportType type, LocalDate startDate,
                              LocalDate endDate, ReportStatus status, String resultLocation, String resultData,
                              String errorMessage, Instant createdAt, Instant startedAt, Instant completedAt) {
        this.id = id;
        this.businessId = businessId;
        this.requestedBy = requestedBy;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.resultLocation = resultLocation;
        this.resultData = resultData;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
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
}
