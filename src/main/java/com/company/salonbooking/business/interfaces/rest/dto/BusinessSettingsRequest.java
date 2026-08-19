package com.company.salonbooking.business.interfaces.rest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record BusinessSettingsRequest(
        @PositiveOrZero int minimumAdvanceMinutes,
        @Positive int maximumAdvanceDays,
        @PositiveOrZero int cancellationMinimumMinutes,
        @Positive int slotIntervalMinutes
) {}