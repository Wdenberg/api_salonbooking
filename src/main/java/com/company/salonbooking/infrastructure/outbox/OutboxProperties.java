package com.company.salonbooking.infrastructure.outbox;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.outbox")
public record OutboxProperties(
        int batchSize,
        int maxAttempts,
        long initialBackoffSeconds,
        long maxBackoffSeconds,
        long pollIntervalMillis
) {
    public OutboxProperties {
        if (batchSize <= 0) batchSize = 50;
        if (maxAttempts <= 0) maxAttempts = 5;
        if (initialBackoffSeconds <= 0) initialBackoffSeconds = 5;
        if (maxBackoffSeconds <= 0) maxBackoffSeconds = 300;
        if (pollIntervalMillis <= 0) pollIntervalMillis = 5000;
    }
}