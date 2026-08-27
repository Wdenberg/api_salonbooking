package com.company.salonbooking.infrastructure.outbox;

import com.company.salonbooking.infrastructure.messaging.MessageBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * Consulta a tabela de *outbox* e tenta entregar eventos pendentes (Seção 27). Múltiplas
 * instâncias desta aplicação podem executar esse mesmo método anotado com @Scheduled simultaneamente sem
 * causar publicação duplicada: a cláusula `FOR UPDATE SKIP LOCKED` (em `OutboxEventJpaRepository.lockNextBatch`)
 * garante que cada instância processe apenas linhas que não estejam sendo mantidas por outra instância no momento.
 *
 * Nota sobre agendamento distribuído (Seção 111): o uso isolado de @Scheduled não impede que todas
 * as instâncias executem `dispatchBatch()` ao mesmo tempo — mas isso não é um problema neste caso específico,
 * pois o `SKIP LOCKED` já torna a execução concorrente segura por design. O uso de ShedLock ou ferramenta similar
 * só seria necessário para tarefas agendadas que *não* fossem inerentemente idempotentes ou seguras quanto a bloqueios
 * (ao contrário desta) — uma observação documentada aqui em vez de implementada preventivamente (Seção 148, YAGNI).
 */

@Component
@EnableConfigurationProperties(OutboxProperties.class)
public class OutboxPublisherJob {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisherJob.class);

    private final OutboxEventJpaRepository repository;
    private final MessageBroker messageBroker;
    private final OutboxProperties properties;
    private final Clock clock;

    public OutboxPublisherJob(OutboxEventJpaRepository repository, MessageBroker messageBroker,
                              OutboxProperties properties, Clock clock) {
        this.repository = repository;
        this.messageBroker = messageBroker;
        this.properties = properties;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-millis:5000}")
    public void publishPendingEvents() {
        dispatchBatch();
    }

    @Transactional
    public void dispatchBatch() {
        Instant now = Instant.now(clock);
        List<OutboxEventJpaEntity> batch = repository.lockNextBatch(now, properties.batchSize());

        for (OutboxEventJpaEntity entity : batch) {
            dispatchOne(entity, now);
        }
    }

    private void dispatchOne(OutboxEventJpaEntity entity, Instant now) {
        try {
            messageBroker.publish(
                    entity.getEventType(),
                    entity.getId(),
                    entity.getAggregateId(),
                    entity.getPayload());
            entity.markPublished(now);
        } catch (Exception e) {
            long backoff = OutboxBackoffCalculator.computeBackoffSeconds(
                    entity.getAttempts() + 1, properties.initialBackoffSeconds(), properties.maxBackoffSeconds());

            entity.registerFailure(now, backoff, properties.maxAttempts());

            log.warn("Failed to publish outbox event id={} type={} attempt={}/{}: {}",
                    entity.getId(), entity.getEventType(), entity.getAttempts(), properties.maxAttempts(), e.getMessage());
        }
        // No explicit save() call needed: entity is a managed JPA entity within this
        // @Transactional method, so field mutations above are flushed automatically at commit.
    }
}