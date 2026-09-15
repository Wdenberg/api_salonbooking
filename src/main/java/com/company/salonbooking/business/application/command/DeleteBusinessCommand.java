package com.company.salonbooking.business.application.command;

import java.util.UUID;

public record DeleteBusinessCommand(UUID businessId, UUID requesterId) {}