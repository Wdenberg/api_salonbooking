package com.company.salonbooking.infrastructure.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {

    /**
     * O mecanismo central da "proteção contra múltiplos publishers" da Seção 27 — `FOR UPDATE SKIP LOCKED` —
     * permite que várias instâncias da aplicação realizem a busca (*polling*) de forma concorrente:
     * cada uma obtém um lote de linhas distinto, sem que nenhuma fique bloqueada aguardando linhas
     * já reivindicadas por outra instância. O bloqueio é mantido durante toda a transação
     * do chamador (veja `OutboxPublisherJob.dispatchBatch`).
     */

    @Query(value = "SELECT * FROM outbox_events " +
            "WHERE status = 'PENDING' AND next_attempt_at <= :now " +
            "ORDER BY created_at ASC " +
            "LIMIT :batchSize " +
            "FOR UPDATE SKIP LOCKED",
            nativeQuery = true)
    List<OutboxEventJpaEntity> lockNextBatch(@Param("now") Instant now, @Param("batchSize") int batchSize);

    List<OutboxEventJpaEntity> findByAggregateTypeAndAggregateId(String aggregateType, UUID aggregateId);
}