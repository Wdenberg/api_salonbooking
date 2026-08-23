package com.company.salonbooking.employee.interfaces.rest.dto;

import com.company.salonbooking.employee.domain.model.AvailabilityBlock;

import java.time.Instant;
import java.util.UUID;

public record AvailabilityBlockResponse(UUID id, UUID employeeId, Instant startAt, Instant endAt, String reason) {
    public static AvailabilityBlockResponse from(AvailabilityBlock block) {
        return new AvailabilityBlockResponse(block.getId(), block.getEmployeeId(), block.getStartAt(),
                block.getEndAt(), block.getReason());
    }
}