package com.company.salonbooking.catalog.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ChangeServiceStatusRequest(
        @Schema(description = "Whether the service is active", example = "true")
        @NotNull Boolean active
) {}
