package com.company.salonbooking.notification.infrastructure.reminder;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configurable reminder offsets (Seção 34: "Esses valores devem ser configuráveis").
 * Kept as a single global configuration rather than per-business (Seção 133 does not
 * list reminder timing among BusinessSettings) — a deliberate YAGNI call (Seção 148);
 * per-business reminder timing can be added later without changing this job's shape.
 */
@ConfigurationProperties(prefix = "app.reminders")
public record ReminderProperties(long h24OffsetMinutes, long h2OffsetMinutes, long pollIntervalMillis) {

    public ReminderProperties {
        if (h24OffsetMinutes <= 0) h24OffsetMinutes = 24 * 60;
        if (h2OffsetMinutes <= 0) h2OffsetMinutes = 2 * 60;
        if (pollIntervalMillis <= 0) pollIntervalMillis = 5 * 60 * 1000; // 5 minutes
    }
}