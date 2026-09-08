package com.company.salonbooking.reporting.application.usecase;

import com.company.salonbooking.audit.domain.model.AuditAction;
import com.company.salonbooking.business.domain.exception.BusinessNotFoundException;
import com.company.salonbooking.business.domain.model.Business;
import com.company.salonbooking.business.domain.repository.BusinessRepository;
import com.company.salonbooking.reporting.application.command.GenerateReportCommand;
import com.company.salonbooking.reporting.domain.event.ReportRequestedEvent;
import com.company.salonbooking.reporting.domain.exception.InvalidReportRequestException;
import com.company.salonbooking.reporting.domain.model.ReportJob;
import com.company.salonbooking.reporting.domain.repository.ReportJobRepository;
import com.company.salonbooking.shared.application.port.AuditRecorder;
import com.company.salonbooking.shared.application.port.DomainEventPublisher;
import com.company.salonbooking.shared.exception.UnauthorizedResourceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

/**
 * POST /reports must never block on the actual report generation (Seção 35, 90 días -
 * "Não bloquear a requisição HTTP durante geração de relatório pesado"): this use case
 * only creates the PENDING ReportJob and publishes ReportRequestedEvent through the
 * same transactional outbox used since Fase 8. The ReportGenerationConsumer (RabbitMQ)
 * does the actual heavy lifting asynchronously.
 */
@Service
public class GenerateReportUseCase {

    private static final int MAX_RANGE_DAYS = 366;

    private final BusinessRepository businessRepository;
    private final ReportJobRepository reportJobRepository;
    private final DomainEventPublisher domainEventPublisher;
    private final AuditRecorder auditRecorder;
    private final Clock clock;

    public GenerateReportUseCase(BusinessRepository businessRepository, ReportJobRepository reportJobRepository,
                                 DomainEventPublisher domainEventPublisher, AuditRecorder auditRecorder, Clock clock) {
        this.businessRepository = businessRepository;
        this.reportJobRepository = reportJobRepository;
        this.domainEventPublisher = domainEventPublisher;
        this.auditRecorder = auditRecorder;
        this.clock = clock;
    }

    @Transactional
    public ReportJob execute(GenerateReportCommand command) {
        Business business = businessRepository.findById(command.businessId())
                .orElseThrow(() -> new BusinessNotFoundException(command.businessId()));

        if (!business.isOwnedBy(command.requesterId())) {
            throw new UnauthorizedResourceException("You do not own this business.");
        }

        validateRange(command.startDate(), command.endDate());

        Instant now = Instant.now(clock);
        ReportJob job = ReportJob.request(UUID.randomUUID(), business.getId(), command.requesterId(),
                command.type(), command.startDate(), command.endDate(), now);

        ReportJob saved = reportJobRepository.save(job);

        domainEventPublisher.publish(new ReportRequestedEvent(
                saved.getId(), saved.getBusinessId(), saved.getType().name(), saved.getStartDate(), saved.getEndDate()));
        auditRecorder.record(command.requesterId(), saved.getBusinessId(), AuditAction.GENERATE_REPORT,
                "ReportJob", saved.getId(), "{\"type\":\"" + saved.getType() + "\"}");
        return saved;
    }

    private void validateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new InvalidReportRequestException("startDate must not be after endDate.");
        }
        if (Period.between(startDate, endDate).getDays() + Period.between(startDate, endDate).getMonths() * 31 > MAX_RANGE_DAYS) {
            // Simple guard against unbounded, expensive report windows.
        }
        if (startDate.plusDays(MAX_RANGE_DAYS).isBefore(endDate)) {
            throw new InvalidReportRequestException("Report range cannot exceed " + MAX_RANGE_DAYS + " days.");
        }
    }
}