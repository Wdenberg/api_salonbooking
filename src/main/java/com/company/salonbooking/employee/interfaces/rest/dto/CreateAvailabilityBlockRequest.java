package com.company.salonbooking.employee.interfaces.rest.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateAvailabilityBlockRequest(@NotNull @Future Instant startAt, @NotNull Instant endAt,
                                             @Size(max = 200) String reason) {}