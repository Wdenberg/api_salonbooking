package com.company.salonbooking.business.application.command;

import java.util.UUID;

public record UpdateBusinessSettingsCommand(
        UUID businessId, UUID requesterId,
        int minimumAdvanceMinutes, int maximumAdvanceDays,
        int cancellationMinimumMinutes, int slotIntervalMinutes
) {}