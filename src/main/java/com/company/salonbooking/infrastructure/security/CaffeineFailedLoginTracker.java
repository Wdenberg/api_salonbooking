package com.company.salonbooking.infrastructure.security;

import com.company.salonbooking.identity.application.port.FailedLoginTracker;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@ConfigurationProperties(prefix = "app.security.login-protection")
public class CaffeineFailedLoginTracker implements FailedLoginTracker {

    private int maxAttempts = 5;
    private long lockoutDurationMinutes = 15;
    private final ConcurrentMap<String, AttemptRecord> attempts = new ConcurrentHashMap<>();

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public void setLockoutDurationMinutes(long lockoutDurationMinutes) {
        this.lockoutDurationMinutes = lockoutDurationMinutes;
    }

    @Override
    public void recordFailure(String key) {
        AttemptRecord record = attempts.compute(key, (k, existing) -> {
            if (existing == null) {
                return new AttemptRecord(1, System.currentTimeMillis() + Duration.ofMinutes(lockoutDurationMinutes).toMillis());
            }
            if (existing.isLocked(maxAttempts)) {
                return existing;
            }
            int newCount = existing.count + 1;
            long lockUntil = newCount >= maxAttempts 
                ? System.currentTimeMillis() + Duration.ofMinutes(lockoutDurationMinutes).toMillis()
                : existing.lockUntil;
            return new AttemptRecord(newCount, lockUntil);
        });
    }

    @Override
    public void recordSuccess(String key) {
        attempts.remove(key);
    }

    @Override
    public boolean isLocked(String key) {
        AttemptRecord record = attempts.get(key);
        if (record == null) {
            return false;
        }
        if (record.isLocked(maxAttempts)) {
            if (System.currentTimeMillis() > record.lockUntil) {
                attempts.remove(key);
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public int getFailureCount(String key) {
        AttemptRecord record = attempts.get(key);
        return record != null ? record.count : 0;
    }

    private static class AttemptRecord {
        final int count;
        final long lockUntil;

        AttemptRecord(int count, long lockUntil) {
            this.count = count;
            this.lockUntil = lockUntil;
        }

        boolean isLocked(int maxAttempts) {
            return count >= maxAttempts && System.currentTimeMillis() <= lockUntil;
        }
    }
}