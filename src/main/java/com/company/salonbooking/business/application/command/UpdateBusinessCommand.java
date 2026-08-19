package com.company.salonbooking.business.application.command;

import com.company.salonbooking.business.domain.model.Address;

import java.util.UUID;

public record UpdateBusinessCommand(
        UUID businessId, UUID requesterId, String name, String description, String phone, String email, Address address
) {}