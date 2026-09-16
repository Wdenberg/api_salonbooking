package com.company.salonbooking.identity.interfaces.rest;

import com.company.salonbooking.identity.application.command.LoginCommand;
import com.company.salonbooking.identity.application.command.LogoutCommand;
import com.company.salonbooking.identity.application.command.RefreshTokenCommand;
import com.company.salonbooking.identity.application.command.RegisterCustomerCommand;
import com.company.salonbooking.identity.application.command.RegisterOwnerCommand;
import com.company.salonbooking.identity.application.dto.AuthResult;
import com.company.salonbooking.identity.application.usecase.LoginUseCase;
import com.company.salonbooking.identity.application.usecase.LogoutUseCase;
import com.company.salonbooking.identity.application.usecase.RefreshTokenUseCase;
import com.company.salonbooking.identity.application.usecase.RegisterCustomerUseCase;
import com.company.salonbooking.identity.application.usecase.RegisterOwnerUseCase;
import com.company.salonbooking.identity.interfaces.rest.dto.AuthResponse;
import com.company.salonbooking.identity.interfaces.rest.dto.LoginRequest;
import com.company.salonbooking.identity.interfaces.rest.dto.LogoutRequest;
import com.company.salonbooking.identity.interfaces.rest.dto.RefreshTokenRequest;
import com.company.salonbooking.identity.interfaces.rest.dto.RegisterCustomerRequest;
import com.company.salonbooking.identity.interfaces.rest.dto.RegisterOwnerRequest;
import com.company.salonbooking.shared.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication, registration, and token management")
public class AuthController {

    private final RegisterOwnerUseCase registerOwnerUseCase;
    private final RegisterCustomerUseCase registerCustomerUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;

    public AuthController(RegisterOwnerUseCase registerOwnerUseCase, RegisterCustomerUseCase registerCustomerUseCase,
                          LoginUseCase loginUseCase, RefreshTokenUseCase refreshTokenUseCase,
                          LogoutUseCase logoutUseCase) {
        this.registerOwnerUseCase = registerOwnerUseCase;
        this.registerCustomerUseCase = registerCustomerUseCase;
        this.loginUseCase = loginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
    }

    @Operation(summary = "Register a new business owner", description = "Creates a new owner account with OWNER role and returns authentication tokens")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Owner registered successfully",
                content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "409", description = "Email already exists",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/register/owner")
    public ResponseEntity<AuthResponse> registerOwner(@Valid @RequestBody RegisterOwnerRequest request) {
        AuthResult result = registerOwnerUseCase.execute(
                new RegisterOwnerCommand(request.name(), request.email(), request.password()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthResponse.of(result.userId(), result.accessToken(), result.refreshToken(), result.accessTokenExpiresInSeconds()));
    }

    @Operation(summary = "Register a new customer", description = "Creates a new customer account with CUSTOMER role and returns authentication tokens")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Customer registered successfully",
                content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "409", description = "Email already exists",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/register/customer")
    public ResponseEntity<AuthResponse> registerCustomer(@Valid @RequestBody RegisterCustomerRequest request) {
        AuthResult result = registerCustomerUseCase.execute(
                new RegisterCustomerCommand(request.name(), request.email(), request.password()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthResponse.of(result.userId(), result.accessToken(), result.refreshToken(), result.accessTokenExpiresInSeconds()));
    }

    @Operation(summary = "Authenticate user and obtain tokens", description = "Validates credentials and returns access/refresh tokens for authenticated sessions")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful",
                content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "429", description = "Account locked due to too many failed attempts",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResult result = loginUseCase.execute(new LoginCommand(request.email(), request.password()));
        return ResponseEntity.ok(AuthResponse.of(result.userId(), result.accessToken(), result.refreshToken(), result.accessTokenExpiresInSeconds()));
    }

    @Operation(summary = "Refresh access token using refresh token", description = "Rotates refresh token and issues new access/refresh token pair")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResult result = refreshTokenUseCase.execute(new RefreshTokenCommand(request.refreshToken()));
        return ResponseEntity.ok(AuthResponse.of(result.userId(), result.accessToken(), result.refreshToken(), result.accessTokenExpiresInSeconds()));
    }

    @Operation(summary = "Logout user and revoke tokens", description = "Revokes the refresh token and adds access token to blocklist for immediate invalidation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Logout successful"),
        @ApiResponse(responseCode = "400", description = "Invalid request",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        logoutUseCase.execute(new LogoutCommand(request.accessToken(), request.refreshToken()));
        return ResponseEntity.noContent().build();
    }
}