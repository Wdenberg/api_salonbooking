package com.company.salonbooking.notification.infrastructure.reminder;

public enum ReminderType {
    H24("24 horas antes"),
    H2("2 horas antes");

    private final String label;

    ReminderType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}