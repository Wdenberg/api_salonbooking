package com.company.salonbooking.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Central registry of custom business counters (Seção 62). Kept as a single class with
 * named methods rather than scattering MeterRegistry.counter(...) calls with hand-typed
 * string literals across use cases — typos in metric names are a common, silent failure
 * mode that this avoids.
 */
@Component
public class AppMetrics {

    private final Counter appointmentsCreated;
    private final Counter appointmentsCancelled;
    private final Counter appointmentsCompleted;
    private final Counter appointmentsConflicts;
    private final Counter rabbitMessagesFailed;
    private final Counter notificationsSent;
    private final Counter notificationsFailed;
    private final Counter reportJobsCompleted;
    private final Counter reportJobsFailed;

    public AppMetrics(MeterRegistry registry) {
        this.appointmentsCreated = Counter.builder("appointments.created")
                .description("Number of appointments successfully created").register(registry);
        this.appointmentsCancelled = Counter.builder("appointments.cancelled")
                .description("Number of appointments cancelled").register(registry);
        this.appointmentsCompleted = Counter.builder("appointments.completed")
                .description("Number of appointments marked completed").register(registry);
        this.appointmentsConflicts = Counter.builder("appointments.conflicts")
                .description("Number of booking attempts rejected due to a time-slot conflict").register(registry);
        this.rabbitMessagesFailed = Counter.builder("rabbitmq.messages.failed")
                .description("Number of message processing failures across all consumers").register(registry);
        this.notificationsSent = Counter.builder("notifications.sent")
                .description("Number of notifications successfully sent").register(registry);
        this.notificationsFailed = Counter.builder("notifications.failed")
                .description("Number of notification send failures").register(registry);
        this.reportJobsCompleted = Counter.builder("report.jobs.completed")
                .description("Number of report jobs completed successfully").register(registry);
        this.reportJobsFailed = Counter.builder("report.jobs.failed")
                .description("Number of report jobs that failed to generate").register(registry);
    }

    public void incrementAppointmentCreated() { appointmentsCreated.increment(); }
    public void incrementAppointmentCancelled() { appointmentsCancelled.increment(); }
    public void incrementAppointmentCompleted() { appointmentsCompleted.increment(); }
    public void incrementAppointmentConflict() { appointmentsConflicts.increment(); }
    public void incrementRabbitMessageFailed(String consumer) {
        rabbitMessagesFailed.increment();
    }
    public void incrementNotificationSent() { notificationsSent.increment(); }
    public void incrementNotificationFailed() { notificationsFailed.increment(); }
    public void incrementReportJobCompleted() { reportJobsCompleted.increment(); }
    public void incrementReportJobFailed() { reportJobsFailed.increment(); }
}