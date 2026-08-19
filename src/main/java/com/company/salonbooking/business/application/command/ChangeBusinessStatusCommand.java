package com.company.salonbooking.business.application.command;

import com.company.salonbooking.business.domain.model.BusinessStatus;

import java.util.UUID;

public record ChangeBusinessStatusCommand(UUID businessId, UUID requesterId, BusinessStatus newStatus) {}