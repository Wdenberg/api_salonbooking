package com.company.salonbooking.reporting.application.usecase;

import com.company.salonbooking.reporting.application.port.ReportGenerator;
import com.company.salonbooking.reporting.domain.exception.ReportJobNotFoundException;
import com.company.salonbooking.reporting.domain.model.ReportJob;
import com.company.salonbooking.reporting.domain.repository.ReportJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Called by ReportGenerationConsumer (infrastructure/messaging) after receiving a
 * ReportRequestedEvent. Delegates the actual data crunching to the ReportGenerator
 * strategy matching the job's type (Seção 35's per-type flow).
 */
@Service
public class ProcessReportUseCase {

    private final ReportJobRepository reportJobRepository;
    private final Map<com.company.salonbooking.reporting.domain.model.ReportType, ReportGenerator> generatorsByType;
    private final Clock clock;

    public ProcessReportUseCase(ReportJobRepository reportJobRepository, List<ReportGenerator> generators, Clock clock) {
        this.reportJobRepository = reportJobRepository;
        this.generatorsByType = generators.stream()
                .collect(java.util.stream.Collectors.toMap(ReportGenerator::supports, g -> g));
        this.clock = clock;
    }

    @Transactional
    public void execute(UUID reportJobId) {
        ReportJob job = reportJobRepository.findById(reportJobId)
                .orElseThrow(() -> new ReportJobNotFoundException(reportJobId));

        // Idempotency guard: if this message is redelivered after the job already
        // finished (dedup at the consumer level already helps, but this is a second,
        // domain-level line of defense), skip silently rather than reprocessing.
        if (job.getStatus() != com.company.salonbooking.reporting.domain.model.ReportStatus.PENDING) {
            return;
        }

        job.markProcessing(Instant.now(clock));
        reportJobRepository.save(job);

        ReportGenerator generator = generatorsByType.get(job.getType());
        if (generator == null) {
            job.markFailed("No generator registered for report type " + job.getType(), Instant.now(clock));
            reportJobRepository.save(job);
            return;
        }

        try {
            String resultData = generator.generate(job);
            String resultLocation = "inline://report-jobs/" + job.getId();
            job.markCompleted(resultLocation, resultData, Instant.now(clock));
        } catch (Exception e) {
            job.markFailed("Report generation failed: " + e.getMessage(), Instant.now(clock));
        }

        reportJobRepository.save(job);
    }
}