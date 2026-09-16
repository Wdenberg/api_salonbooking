package com.company.salonbooking.admin.interfaces.rest;

import com.company.salonbooking.admin.interfaces.rest.dto.AdminUserResponse;
import com.company.salonbooking.identity.application.usecase.ListAllUsersUseCase;
import com.company.salonbooking.identity.domain.model.User;
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
@Tag(name = "Admin - Users", description = "Platform admin endpoints for cross-tenant user management")
public class AdminUserController {

    private final ListAllUsersUseCase listAllUsersUseCase;

    public AdminUserController(ListAllUsersUseCase listAllUsersUseCase) {
        this.listAllUsersUseCase = listAllUsersUseCase;
    }

    @Operation(summary = "List all users (cross-tenant)", description = "Returns a paginated list of all users across all businesses. Only accessible by PLATFORM_ADMIN.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully",
                content = @Content(schema = @Schema(implementation = AdminUserResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthenticated",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - not a platform admin",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/users")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<List<AdminUserResponse>> listAll(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<User> users = listAllUsersUseCase.execute(page, size);

        List<AdminUserResponse> response = users.stream()
                .map(AdminUserResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }
}