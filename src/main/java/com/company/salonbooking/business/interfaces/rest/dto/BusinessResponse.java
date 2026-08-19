package com.company.salonbooking.business.interfaces.rest.dto;

import com.company.salonbooking.business.domain.model.Business;
import com.company.salonbooking.business.domain.model.BusinessStatus;

import java.time.Instant;
import java.util.UUID;

public record BusinessResponse(
        UUID id, UUID ownerId, String name, String description, String phone, String email,
        AddressDto address, String timezone, BusinessStatus status, Instant createdAt, Instant updatedAt
) {
    public static BusinessResponse from(Business business) {
        var address = business.getAddress();
        return new BusinessResponse(
                business.getId(), business.getOwnerId(), business.getName(), business.getDescription(),
                business.getPhone(), business.getEmail(),
                new AddressDto(address.street(), address.number(), address.city(), address.state(),
                        address.zipCode(), address.country()),
                business.getTimezone().getId(), business.getStatus(), business.getCreatedAt(), business.getUpdatedAt());
    }
}