package com.company.salonbooking.catalog.application.command;

import java.util.UUID;

public record ChangeServiceStatusCommand(UUID serviceId, UUID requesterId, boolean active) {}