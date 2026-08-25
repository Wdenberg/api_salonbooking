package com.company.salonbooking.catalog.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateServiceCommand(UUID serviceId, UUID requesterId, String name, String description,
                                   BigDecimal priceAmount, String priceCurrency, int durationMinutes) {}