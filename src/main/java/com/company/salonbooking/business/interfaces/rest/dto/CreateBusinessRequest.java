package com.company.salonbooking.business.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBusinessRequest(
        @Schema(description = "Business name", example = "Barbearia do João")
        @NotBlank @Size(max = 150) String name,
        @Schema(description = "Business description", example = "Traditional barbershop since 1990")
        @Size(max = 1000) String description,
        @Schema(description = "Business phone number", example = "+55 11 99999-9999")
        String phone,
        @Schema(description = "Business email", example = "contato@barbeariadojoao.com")
        String email,
        @Valid AddressDto address,
        @Schema(description = "IANA timezone identifier", example = "America/Sao_Paulo")
        @NotBlank String timezone
) {}
