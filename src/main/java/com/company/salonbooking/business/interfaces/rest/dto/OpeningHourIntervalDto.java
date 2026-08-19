package com.company.salonbooking.business.interfaces.rest.dto;

import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record OpeningHourIntervalDto(@NotNull DayOfWeek dayOfWeek, @NotNull LocalTime openTime, @NotNull LocalTime closeTime) {}