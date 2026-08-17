package com.company.salonbooking.identity.interfaces.rest;

import com.company.salonbooking.identity.application.command.LoginCommand;
import com.company.salonbooking.identity.application.command.RegisterCustomerCommand;
import com.company.salonbooking.identity.application.command.RegisterOwnerCommand;
import com.company.salonbooking.identity.application.dto.AuthResult;
import com.company.salonbooking.identity.application.usecase.LoginUseCase;
import com.company.salonbooking.identity.application.usecase.RegisterCustomerUseCase;
import com.company.salonbooking.identity.application.usecase.RegisterOwnerUseCase;
import com.company.salonbooking.identity.interfaces.rest.dto.AuthResponse;
import com.company.salonbooking.identity.interfaces.rest.dto.LoginRequest;
import com.company.salonbooking.identity.interfaces.rest.dto.RegisterCustomerRequest;
import com.company.salonbooking.identity.interfaces.rest.dto.RegisterOwnerRequest;
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
@Tag(name = "Authentication")
public class AuthController {

    private final RegisterOwnerUseCase registerOwnerUseCase;
    private final RegisterCustomerUseCase registerCustomerUseCase;
    private final LoginUseCase loginUseCase;

    public AuthController(RegisterOwnerUseCase registerOwnerUseCase, RegisterCustomerUseCase registerCustomerUseCase,
                          LoginUseCase loginUseCase) {
        this.registerOwnerUseCase = registerOwnerUseCase;
        this.registerCustomerUseCase = registerCustomerUseCase;
        this.loginUseCase = loginUseCase;
    }

    @PostMapping("/register/owner")
    public ResponseEntity<AuthResponse> registerOwner(@Valid @RequestBody RegisterOwnerRequest request) {
        AuthResult result = registerOwnerUseCase.execute(
                new RegisterOwnerCommand(request.name(), request.email(), request.password()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthResponse.of(result.userId(), result.accessToken(), result.expiresInSeconds()));
    }

    @PostMapping("/register/customer")
    public ResponseEntity<AuthResponse> registerCustomer(@Valid @RequestBody RegisterCustomerRequest request) {
        AuthResult result = registerCustomerUseCase.execute(
                new RegisterCustomerCommand(request.name(), request.email(), request.password()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthResponse.of(result.userId(), result.accessToken(), result.expiresInSeconds()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResult result = loginUseCase.execute(new LoginCommand(request.email(), request.password()));
        return ResponseEntity.ok(AuthResponse.of(result.userId(), result.accessToken(), result.expiresInSeconds()));
    }
}