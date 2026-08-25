package com.company.salonbooking.catalog.interfaces.rest.dto;

import jakarta.validation.constraints.NotNull;

public record ChangeServiceStatusRequest(@NotNull Boolean active) {}