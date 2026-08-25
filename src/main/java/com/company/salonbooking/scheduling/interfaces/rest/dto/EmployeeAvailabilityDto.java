package com.company.salonbooking.scheduling.interfaces.rest.dto;

import java.util.List;
import java.util.UUID;

public record EmployeeAvailabilityDto(UUID employeeId, List<TimeSlotDto> slots) {}