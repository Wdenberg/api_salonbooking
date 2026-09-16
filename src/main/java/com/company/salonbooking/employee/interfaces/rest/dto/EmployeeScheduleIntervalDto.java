package com.company.salonbooking.employee.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record EmployeeScheduleIntervalDto(
        @Schema(description = "Day of the week", example = "MONDAY")
        @NotNull DayOfWeek dayOfWeek,
        @Schema(description = "Start time", example = "09:00:00")
        @NotNull LocalTime startTime,
        @Schema(description = "End time", example = "18:00:00")
        @NotNull LocalTime endTime
) {}
