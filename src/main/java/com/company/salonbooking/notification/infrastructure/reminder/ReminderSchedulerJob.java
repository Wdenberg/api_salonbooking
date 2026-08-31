package com.company.salonbooking.notification.infrastructure.reminder;

import com.company.salonbooking.scheduling.domain.event.AppointmentReminderRequestedEvent;
import com.company.salonbooking.scheduling.domain.model.Appointment;
import com.company.salonbooking.scheduling.domain.repository.AppointmentRepository;
import com.company.salonbooking.shared.application.port.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * Finds CONFIRMED appointments entering a reminder window and publishes one
 * AppointmentReminderRequestedEvent per (appointment, reminderType) pair — exactly
 * once, even across multiple polling cycles or multiple application instances
 * (Seção 34's idempotency requirement, Seção 111's multi-instance note).
 *
 * Each appointment/reminder-type combination is processed in its own REQUIRES_NEW
 * transaction: the dedup-log insert and the outbox publish happen atomically together,
 * and a PostgreSQL unique-constraint violation on the dedup log (a concurrent poller,
 * or this same poller re-selecting a not-yet-committed row) simply skips that one
 * appointment without aborting the rest of the batch — the same defensive pattern as
 * OutboxPublisherJob (Fase 8) and IdempotencyService (Fase 7).
 *
 * Not using ShedLock here either: unlike a job that MUST run exactly once cluster-wide,
 * this job is safe to run concurrently on every instance because the real exclusivity
 * guarantee lives in the database constraint, not in scheduling coordination (Seção 111).
 */
@Component
@EnableConfigurationProperties(ReminderProperties.class)
public class ReminderSchedulerJob {

    private static final Logger log = LoggerFactory.getLogger(ReminderSchedulerJob.class);

    private final AppointmentRepository appointmentRepository;
    private final ReminderDispatchLogJpaRepository dispatchLogRepository;
    private final DomainEventPublisher domainEventPublisher;
    private final ReminderProperties properties;
    private final TransactionTemplate requiresNewTransactionTemplate;
    private final Clock clock;

    public ReminderSchedulerJob(AppointmentRepository appointmentRepository, ReminderDispatchLogJpaRepository dispatchLogRepository,
                                DomainEventPublisher domainEventPublisher, ReminderProperties properties,
                                PlatformTransactionManager transactionManager, Clock clock) {
        this.appointmentRepository = appointmentRepository;
        this.dispatchLogRepository = dispatchLogRepository;
        this.domainEventPublisher = domainEventPublisher;
        this.properties = properties;
        this.requiresNewTransactionTemplate = new TransactionTemplate(transactionManager);
        this.requiresNewTransactionTemplate.setPropagationBehavior(
                org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${app.reminders.poll-interval-millis:300000}")
    public void dispatchDueReminders() {
        Instant now = Instant.now(clock);
        dispatchForWindow(ReminderType.H24, now, properties.h24OffsetMinutes());
        dispatchForWindow(ReminderType.H2, now, properties.h2OffsetMinutes());
    }

    private void dispatchForWindow(ReminderType type, Instant now, long offsetMinutes) {
        Instant windowEnd = now.plusSeconds(offsetMinutes * 60);

        List<Appointment> eligible = appointmentRepository.findConfirmedStartingBetween(now, windowEnd);

        for (Appointment appointment : eligible) {
            boolean dispatched = tryDispatchOne(appointment, type, now);
            if (dispatched) {
                log.info("Reminder {} scheduled for appointment {}", type, appointment.getId());
            }
        }
    }

    private boolean tryDispatchOne(Appointment appointment, ReminderType type, Instant now) {
        try {
            return requiresNewTransactionTemplate.execute(status -> {
                ReminderDispatchLogId id = new ReminderDispatchLogId(appointment.getId(), type);
                dispatchLogRepository.saveAndFlush(new ReminderDispatchLogJpaEntity(id, now));

                domainEventPublisher.publish(new AppointmentReminderRequestedEvent(
                        appointment.getId(), appointment.getBusinessId(), appointment.getCustomerId(),
                        appointment.getStartAt(), type.name()));

                return true;
            });
        } catch (DataIntegrityViolationException e) {
            // Already dispatched by this or another poll cycle/instance — expected, not an error.
            return false;
        }
    }
}