package com.company.salonbooking.business.application.command;

import com.company.salonbooking.business.domain.model.Address;

import java.util.UUID;

public record CreateBusinessCommand(
        UUID ownerId, String name, String description, String phone, String email,
        Address address, String timezone
) {}