package com.company.salonbooking.identity.application.port;

public interface FailedLoginTracker {
    void recordFailure(String key);
    void recordSuccess(String key);
    boolean isLocked(String key);
    int getFailureCount(String key);
}