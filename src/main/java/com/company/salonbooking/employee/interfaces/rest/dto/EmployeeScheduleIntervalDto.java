package com.company.salonbooking.employee.interfaces.rest.dto;

import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record EmployeeScheduleIntervalDto(@NotNull DayOfWeek dayOfWeek, @NotNull LocalTime startTime, @NotNull LocalTime endTime) {}