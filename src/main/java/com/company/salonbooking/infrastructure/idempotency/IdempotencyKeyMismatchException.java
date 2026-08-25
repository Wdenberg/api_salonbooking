package com.company.salonbooking.infrastructure.idempotency;

/** Raised when the same Idempotency-Key is reused with a different request body — a client bug or misuse. */
public class IdempotencyKeyMismatchException extends RuntimeException {

    public IdempotencyKeyMismatchException() {
        super("This Idempotency-Key was already used with a different request payload.");
    }
}