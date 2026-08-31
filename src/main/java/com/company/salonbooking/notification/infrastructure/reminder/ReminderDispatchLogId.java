package com.company.salonbooking.notification.infrastructure.reminder;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ReminderDispatchLogId implements Serializable {

    @Column(name = "appointment_id", nullable = false)
    private UUID appointmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_type", nullable = false, length = 10)
    private ReminderType reminderType;

    protected ReminderDispatchLogId() {
    }

    public ReminderDispatchLogId(UUID appointmentId, ReminderType reminderType) {
        this.appointmentId = appointmentId;
        this.reminderType = reminderType;
    }

    public UUID getAppointmentId() { return appointmentId; }
    public ReminderType getReminderType() { return reminderType; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReminderDispatchLogId that)) return false;
        return appointmentId.equals(that.appointmentId) && reminderType == that.reminderType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(appointmentId, reminderType);
    }
}