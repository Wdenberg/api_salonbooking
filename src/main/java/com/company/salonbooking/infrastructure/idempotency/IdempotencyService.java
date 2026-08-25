package com.company.salonbooking.infrastructure.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Wraps a use case invocation with persistent idempotency (Seção 28). Persistence
 * (not in-memory) is required so the guarantee survives across application instances
 * and restarts (Seção 110).
 *
 * Each step below runs in its own REQUIRES_NEW transaction, deliberately separate from
 * the business transaction executed by `action`:
 *   1. Try to insert an IN_PROGRESS row (fails fast on a concurrent duplicate via the
 *      DB's unique constraint on (key, userId, endpoint) — the actual source of truth).
 *   2. Run the wrapped action in its own (normal) transaction.
 *   3. Record the successful response, or delete the row on failure so the client can
 *      safely retry a failed attempt with the same key.
 */
@Component
public class IdempotencyService {

    private final IdempotencyRecordJpaRepository repository;
    private final TransactionTemplate requiresNewTransactionTemplate;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public IdempotencyService(IdempotencyRecordJpaRepository repository, PlatformTransactionManager transactionManager,
                              ObjectMapper objectMapper, Clock clock) {
        this.repository = repository;
        this.requiresNewTransactionTemplate = new TransactionTemplate(transactionManager);
        this.requiresNewTransactionTemplate.setPropagationBehavior(
                org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    /**
     * @param idempotencyKey   value of the Idempotency-Key header
     * @param userId           authenticated requester (part of the uniqueness scope)
     * @param endpoint         logical endpoint identifier, e.g. "POST /api/v1/appointments"
     * @param requestPayload   the request DTO, hashed to detect key reuse with a different body
     * @param responseType     type to deserialize a replayed response into
     * @param action           the actual business operation to execute exactly once
     */
    public <T> ResponseEntity<T> execute(String idempotencyKey, UUID userId, String endpoint, Object requestPayload,
                                         Class<T> responseType, Supplier<ResponseEntity<T>> action) {
        String fingerprint = computeFingerprint(requestPayload);

        UUID recordId = UUID.randomUUID();
        boolean inserted = tryInsertInProgress(recordId, idempotencyKey, userId, endpoint, fingerprint);

        if (!inserted) {
            return handleExistingRecord(idempotencyKey, userId, endpoint, fingerprint, responseType);
        }

        try {
            ResponseEntity<T> response = action.get();
            markCompleted(recordId, response);
            return response;
        } catch (RuntimeException e) {
            // Allow the client to retry with the same key after a failed attempt.
            deleteRecord(recordId);
            throw e;
        }
    }

    private boolean tryInsertInProgress(UUID recordId, String idempotencyKey, UUID userId, String endpoint, String fingerprint) {
        try {
            requiresNewTransactionTemplate.executeWithoutResult(status -> {
                IdempotencyRecordJpaEntity entity = new IdempotencyRecordJpaEntity(
                        recordId, idempotencyKey, userId, endpoint, fingerprint, IdempotencyStatus.IN_PROGRESS, Instant.now(clock));
                repository.saveAndFlush(entity);
            });
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }

    private <T> ResponseEntity<T> handleExistingRecord(String idempotencyKey, UUID userId, String endpoint,
                                                       String fingerprint, Class<T> responseType) {
        IdempotencyRecordJpaEntity existing = repository
                .findByIdempotencyKeyAndUserIdAndEndpoint(idempotencyKey, userId, endpoint)
                .orElseThrow(() -> new IllegalStateException(
                        "Idempotency record vanished unexpectedly for key: " + idempotencyKey));

        if (!existing.getRequestFingerprint().equals(fingerprint)) {
            throw new IdempotencyKeyMismatchException();
        }

        if (existing.getStatus() == IdempotencyStatus.IN_PROGRESS) {
            throw new IdempotencyKeyInProgressException();
        }

        // COMPLETED with matching fingerprint: replay the original response verbatim.
        T body = deserialize(existing.getResponseBody(), responseType);
        return ResponseEntity.status(HttpStatusCode.valueOf(existing.getResponseStatus())).body(body);
    }

    private <T> void markCompleted(UUID recordId, ResponseEntity<T> response) {
        String json = serialize(response.getBody());
        requiresNewTransactionTemplate.executeWithoutResult(status -> {
            IdempotencyRecordJpaEntity entity = repository.getReferenceById(recordId);
            entity.complete(response.getStatusCode().value(), json, Instant.now(clock));
            repository.save(entity);
        });
    }

    private void deleteRecord(UUID recordId) {
        requiresNewTransactionTemplate.executeWithoutResult(status -> repository.deleteById(recordId));
    }

    private String computeFingerprint(Object payload) {
        return RequestFingerprint.of(serialize(payload));
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize idempotency payload", e);
        }
    }

    private <T> T deserialize(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize stored idempotent response", e);
        }
    }
}