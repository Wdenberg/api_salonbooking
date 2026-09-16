package com.company.salonbooking.business.interfaces.rest.dto;

import com.company.salonbooking.business.domain.model.BusinessStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ChangeBusinessStatusRequest(
        @Schema(description = "Business status", example = "ACTIVE")
        @NotNull BusinessStatus status
) {}
