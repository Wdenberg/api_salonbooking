package com.company.salonbooking.shared.exception;

import com.company.salonbooking.identity.domain.exception.EmailAlreadyExistsException;
import com.company.salonbooking.identity.domain.exception.InvalidCredentialsException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Clock clock;

    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(EmailAlreadyExistsException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(UnauthorizedResourceException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedResource(UnauthorizedResourceException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "RESOURCE_ACCESS_DENIED", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed.", request, errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "An unexpected error occurred.", request, List.of());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message,
                                                HttpServletRequest request, List<String> errors) {
        ErrorResponse body = new ErrorResponse(
                Instant.now(clock),
                status.value(),
                code,
                message,
                request.getRequestURI(),
                MDC.get("correlationId"),
                errors
        );
        return ResponseEntity.status(status).body(body);
    }
    @ExceptionHandler(com.company.salonbooking.business.domain.exception.InvalidOpeningHoursException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOpeningHours(
            com.company.salonbooking.business.domain.exception.InvalidOpeningHoursException ex, HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_OPENING_HOURS", ex.getMessage(), request, List.of());
    }
}