package com.company.salonbooking.infrastructure.idempotency;

/**
 * Raised when a second request arrives with the same Idempotency-Key while the first
 * one is still executing. This implementation does not poll/wait for the in-flight
 * request to finish (Seção 148 — YAGNI); the client is expected to retry shortly.
 */
public class IdempotencyKeyInProgressException extends RuntimeException {

    public IdempotencyKeyInProgressException() {
        super("A request with this Idempotency-Key is already being processed.");
    }
}