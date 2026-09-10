package com.company.salonbooking.business.application.command;

import com.company.salonbooking.business.domain.model.OpeningHourInterval;

import java.util.List;
import java.util.UUID;

public record UpdateOpeningHoursCommand(
        UUID businessId,
        UUID requesterId,
        List<OpeningHourInterval> intervals
) {
    public UpdateOpeningHoursCommand {
        intervals = intervals == null ? List.of() : List.copyOf(intervals);
    }
}
