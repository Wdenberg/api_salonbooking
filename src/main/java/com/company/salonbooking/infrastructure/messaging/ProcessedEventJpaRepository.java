package com.company.salonbooking.infrastructure.messaging;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventJpaRepository extends JpaRepository<ProcessedEventJpaEntity, ProcessedEventId> {

    boolean existsById(ProcessedEventId id);
}