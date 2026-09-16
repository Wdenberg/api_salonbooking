package com.company.salonbooking.catalog.application.command;

import java.util.UUID;

public record DeleteServiceCommand(UUID serviceId, UUID requesterId) {}