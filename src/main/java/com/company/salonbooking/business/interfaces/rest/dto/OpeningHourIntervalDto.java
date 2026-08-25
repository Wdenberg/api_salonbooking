package com.company.salonbooking.business.interfaces.rest.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record OpeningHourIntervalDto(
        @NotNull DayOfWeek dayOfWeek,
        @NotNull @JsonAlias({"startTime", "openTime"}) LocalTime openTime,
        @NotNull @JsonAlias({"endTime", "closeTime"}) LocalTime closeTime
) {}