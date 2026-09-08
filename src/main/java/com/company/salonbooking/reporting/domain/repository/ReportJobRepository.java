package com.company.salonbooking.reporting.domain.repository;

import com.company.salonbooking.reporting.domain.model.ReportJob;

import java.util.Optional;
import java.util.UUID;

public interface ReportJobRepository {

    Optional<ReportJob> findById(UUID id);

    ReportJob save(ReportJob reportJob);
}