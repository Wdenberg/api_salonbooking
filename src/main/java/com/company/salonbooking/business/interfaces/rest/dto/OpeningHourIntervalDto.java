package com.company.salonbooking.business.interfaces.rest.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record OpeningHourIntervalDto(
        @Schema(description = "Day of the week", example = "MONDAY")
        @NotNull DayOfWeek dayOfWeek,
        @Schema(description = "Opening time", example = "09:00:00")
        @NotNull @JsonAlias({"startTime", "openTime"}) LocalTime openTime,
        @Schema(description = "Closing time", example = "18:00:00")
        @NotNull @JsonAlias({"endTime", "closeTime"}) LocalTime closeTime
) {}
