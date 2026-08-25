package com.company.salonbooking.scheduling.interfaces.rest.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record CreateAppointmentRequest(
        @NotNull UUID businessId,
        @NotNull UUID employeeId,
        @NotNull UUID serviceId,
        @NotNull @Future Instant startAt,
        @Size(max = 500) String notes
) {}