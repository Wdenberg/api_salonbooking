package com.company.salonbooking.scheduling.domain.exception;

/** Umbrella exception for business-rule violations that are not a booking conflict:
 * time in the past, outside business hours, outside employee schedule, blocked, etc. */
public class SchedulingRuleViolationException extends RuntimeException {

    public SchedulingRuleViolationException(String message) {
        super(message);
    }
}