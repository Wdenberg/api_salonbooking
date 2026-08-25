package com.company.salonbooking.scheduling.application.query;

import java.time.LocalDate;
import java.util.UUID;

public record GetAvailabilityQuery(UUID businessId, UUID serviceId, UUID employeeId, LocalDate date) {}