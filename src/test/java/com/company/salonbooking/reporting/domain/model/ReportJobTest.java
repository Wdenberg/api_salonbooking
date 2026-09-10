package com.company.salonbooking.reporting.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReportJobTest {

    private ReportJob newJob() {
        return ReportJob.request(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), ReportType.REVENUE,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), Instant.now());
    }

    @Test
    void deveRecusarStartDateAposEndDate() {
        assertThrows(IllegalArgumentException.class, () -> ReportJob.request(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), ReportType.REVENUE, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 1), Instant.now()));
    }

    @Test
    void devePassarPorPendingProcessingCompleted() {
        ReportJob job = newJob();
        assertThat(job.getStatus()).isEqualTo(ReportStatus.PENDING);

        job.markProcessing(Instant.now());
        assertThat(job.getStatus()).isEqualTo(ReportStatus.PROCESSING);

        job.markCompleted("inline://x", "{}", Instant.now());
        assertThat(job.getStatus()).isEqualTo(ReportStatus.COMPLETED);
        assertThat(job.getResultData()).isEqualTo("{}");
    }

    @Test
    void naoDeveCompletarSemEstarProcessing() {
        ReportJob job = newJob();
        assertThrows(IllegalStateException.class, () -> job.markCompleted("x", "{}", Instant.now()));
    }

    @Test
    void deveMarcarFailedAPartirDeQualquerEstadoAtivo() {
        ReportJob job = newJob();
        job.markFailed("boom", Instant.now());
        assertThat(job.getStatus()).isEqualTo(ReportStatus.FAILED);
        assertThat(job.getErrorMessage()).isEqualTo("boom");
    }
}
