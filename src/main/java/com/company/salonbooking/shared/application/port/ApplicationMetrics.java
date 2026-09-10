package com.company.salonbooking.shared.application.port;

public interface ApplicationMetrics {

    void incrementAppointmentCreated();

    void incrementAppointmentCancelled();

    void incrementAppointmentCompleted();

    void incrementAppointmentConflict();

    void incrementRabbitMessageFailed(String consumer);

    void incrementNotificationSent();

    void incrementNotificationFailed();

    void incrementReportJobCompleted();

    void incrementReportJobFailed();
}
