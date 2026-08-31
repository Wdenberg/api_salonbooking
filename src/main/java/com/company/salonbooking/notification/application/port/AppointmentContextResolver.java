package com.company.salonbooking.notification.application.port;

import java.time.Instant;
import java.util.UUID;

/**
 * Resolves the human-readable context needed to compose a notification body, without
 * the notification module depending directly on scheduling's persistence internals.
 * Implemented by an adapter inside scheduling/infrastructure (cross-module read,
 * same pattern as EmployeeNameResolver in Fase 6).
 */
public interface AppointmentContextResolver {

    record AppointmentContext(UUID appointmentId, UUID customerId, UUID employeeId, UUID businessId,
                              String businessName, String serviceName, String employeeName, Instant startAt) {}

    AppointmentContext resolve(UUID appointmentId);
}