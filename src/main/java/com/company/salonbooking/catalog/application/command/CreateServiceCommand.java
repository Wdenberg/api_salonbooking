package com.company.salonbooking.catalog.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateServiceCommand(UUID requesterId, UUID businessId, String name, String description,
                                   BigDecimal priceAmount, String priceCurrency, int durationMinutes) {}