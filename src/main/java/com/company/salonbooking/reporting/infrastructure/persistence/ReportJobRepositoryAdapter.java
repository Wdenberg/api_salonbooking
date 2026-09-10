package com.company.salonbooking.reporting.infrastructure.persistence;

import com.company.salonbooking.reporting.domain.model.ReportJob;
import com.company.salonbooking.reporting.domain.repository.ReportJobRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class ReportJobRepositoryAdapter implements ReportJobRepository {

    private final ReportJobJpaRepository jpaRepository;

    public ReportJobRepositoryAdapter(ReportJobJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<ReportJob> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public ReportJob save(ReportJob reportJob) {
        ReportJobJpaEntity entity = new ReportJobJpaEntity(reportJob.getId(), reportJob.getBusinessId(),
                reportJob.getRequestedBy(), reportJob.getType(), reportJob.getStartDate(), reportJob.getEndDate(),
                reportJob.getStatus(), reportJob.getResultLocation(), reportJob.getResultData(), reportJob.getErrorMessage(),
                reportJob.getCreatedAt(), reportJob.getStartedAt(), reportJob.getCompletedAt());
        return toDomain(jpaRepository.save(entity));
    }

    private ReportJob toDomain(ReportJobJpaEntity e) {
        return ReportJob.restore(e.getId(), e.getBusinessId(), e.getRequestedBy(), e.getType(), e.getStartDate(),
                e.getEndDate(), e.getStatus(), e.getResultLocation(), e.getResultData(), e.getErrorMessage(),
                e.getCreatedAt(), e.getStartedAt(), e.getCompletedAt());
    }
}
