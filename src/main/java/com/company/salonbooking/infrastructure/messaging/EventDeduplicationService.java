package com.company.salonbooking.infrastructure.messaging;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Component
public class EventDeduplicationService {

    private final ProcessedEventJpaRepository repository;
    private final Clock clock;

    public EventDeduplicationService(ProcessedEventJpaRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public boolean alreadyProcessed(UUID eventId, String consumerName) {
        return repository.existsById(new ProcessedEventId(eventId, consumerName));
    }

    public boolean markProcessed(UUID eventId, String consumerName) {
        try {
            ProcessedEventId id = new ProcessedEventId(eventId, consumerName);
            if (repository.existsById(id)) {
                return false;
            }
            repository.saveAndFlush(new ProcessedEventJpaEntity(id, Instant.now(clock)));
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }
}