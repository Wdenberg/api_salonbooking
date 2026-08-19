package com.company.salonbooking.business.domain.repository;

import com.company.salonbooking.business.domain.model.OpeningHourInterval;

import java.util.List;
import java.util.UUID;

public interface BusinessOpeningHourRepository {

    List<OpeningHourInterval> findByBusinessId(UUID businessId);

    /** Replaces the entire weekly schedule atomically (delete-all + insert-all semantics). */
    List<OpeningHourInterval> replaceAll(UUID businessId, List<OpeningHourInterval> intervals);
}