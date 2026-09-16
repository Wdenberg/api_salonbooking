package com.company.salonbooking.scheduling.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

public record TimeSlotDto(
        @Schema(description = "Slot start time", example = "2026-09-20T14:00:00Z")
        Instant start,
        @Schema(description = "Slot end time", example = "2026-09-20T14:30:00Z")
        Instant end
) {}
