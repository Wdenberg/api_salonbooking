package com.company.salonbooking.business.infrastructure.persistence;

import com.company.salonbooking.business.domain.model.Address;
import com.company.salonbooking.business.domain.model.Business;

import java.time.ZoneId;

final class BusinessMapper {

    private BusinessMapper() {
    }

    static Business toDomain(BusinessJpaEntity entity) {
        Address address = new Address(entity.getAddressStreet(), entity.getAddressNumber(), entity.getAddressCity(),
                entity.getAddressState(), entity.getAddressZipCode(), entity.getAddressCountry());

        return Business.restore(entity.getId(), entity.getOwnerId(), entity.getName(), entity.getDescription(),
                entity.getPhone(), entity.getEmail(), address, ZoneId.of(entity.getTimezone()), entity.getStatus(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }

    static BusinessJpaEntity toEntity(Business business) {
        Address address = business.getAddress();
        return new BusinessJpaEntity(
                business.getId(), business.getOwnerId(), business.getName(), business.getDescription(),
                business.getPhone(), business.getEmail(),
                address.street(), address.number(), address.city(), address.state(),
                address.zipCode(), address.country(),
                business.getTimezone().getId(), business.getStatus(),
                business.getCreatedAt(), business.getUpdatedAt());
    }
}