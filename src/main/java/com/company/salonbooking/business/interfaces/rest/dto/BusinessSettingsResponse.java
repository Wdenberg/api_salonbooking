package com.company.salonbooking.business.interfaces.rest.dto;

import com.company.salonbooking.business.domain.model.BusinessSettings;

public record BusinessSettingsResponse(
        int minimumAdvanceMinutes, int maximumAdvanceDays, int cancellationMinimumMinutes, int slotIntervalMinutes
) {
    public static BusinessSettingsResponse from(BusinessSettings settings) {
        return new BusinessSettingsResponse(settings.getMinimumAdvanceMinutes(), settings.getMaximumAdvanceDays(),
                settings.getCancellationMinimumMinutes(), settings.getSlotIntervalMinutes());
    }
}