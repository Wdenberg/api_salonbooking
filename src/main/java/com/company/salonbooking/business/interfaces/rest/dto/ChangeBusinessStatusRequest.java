package com.company.salonbooking.business.interfaces.rest.dto;

import com.company.salonbooking.business.domain.model.BusinessStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeBusinessStatusRequest(@NotNull BusinessStatus status) {}