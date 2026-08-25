package com.company.salonbooking.scheduling.interfaces.rest.dto;

import java.time.Instant;
import java.util.UUID;

public record TimeSlotDto(Instant start, Instant end) {}