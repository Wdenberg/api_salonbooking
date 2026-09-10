package com.company.salonbooking.scheduling.interfaces.rest.dto;

import java.time.Instant;

public record TimeSlotDto(Instant start, Instant end) {}
