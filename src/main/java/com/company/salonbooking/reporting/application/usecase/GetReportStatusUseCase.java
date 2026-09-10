package com.company.salonbooking.reporting.application.usecase;

import com.company.salonbooking.reporting.domain.exception.ReportJobNotFoundException;
import com.company.salonbooking.reporting.domain.model.ReportJob;
import com.company.salonbooking.reporting.domain.repository.ReportJobRepository;
import com.company.salonbooking.shared.exception.UnauthorizedResourceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Enforces ownership (Seção 51): only the OWNER of the business that requested the report can view it. */
@Service
public class GetReportStatusUseCase {

    private final ReportJobRepository reportJobRepository;

    public GetReportStatusUseCase(ReportJobRepository reportJobRepository) {
        this.reportJobRepository = reportJobRepository;
    }

    @Transactional(readOnly = true)
    public ReportJob execute(UUID reportJobId, UUID requesterId) {
        ReportJob job = reportJobRepository.findById(reportJobId)
                .orElseThrow(() -> new ReportJobNotFoundException(reportJobId));

        if (!job.getRequestedBy().equals(requesterId)) {
            throw new UnauthorizedResourceException("You cannot view this report.");
        }

        return job;
    }
}
