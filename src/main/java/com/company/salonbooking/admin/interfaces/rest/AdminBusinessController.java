package com.company.salonbooking.admin.interfaces.rest;

import com.company.salonbooking.admin.interfaces.rest.dto.AdminBusinessResponse;
import com.company.salonbooking.business.application.usecase.ListAllBusinessesUseCase;
import com.company.salonbooking.business.domain.model.Business;
import com.company.salonbooking.infrastructure.security.AuthenticatedUser;
import com.company.salonbooking.shared.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin - Businesses", description = "Platform admin endpoints for cross-tenant business management")
public class AdminBusinessController {

    private final ListAllBusinessesUseCase listAllBusinessesUseCase;

    public AdminBusinessController(ListAllBusinessesUseCase listAllBusinessesUseCase) {
        this.listAllBusinessesUseCase = listAllBusinessesUseCase;
    }

    @Operation(summary = "List all businesses (cross-tenant)", description = "Returns a paginated list of all businesses across all owners. Only accessible by PLATFORM_ADMIN.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Businesses retrieved successfully",
                content = @Content(schema = @Schema(implementation = AdminBusinessResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthenticated",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - not a platform admin",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/businesses")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<List<AdminBusinessResponse>> listAll(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<Business> businesses = listAllBusinessesUseCase.execute(page, size);

        List<AdminBusinessResponse> response = businesses.stream()
                .map(AdminBusinessResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }
}