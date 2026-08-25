package com.company.salonbooking.scheduling.application.port;

import java.util.UUID;

/** Resolves the display name of an employee at booking time, for the Appointment snapshot (Seção 129). */
public interface EmployeeNameResolver {

    String resolveName(UUID employeeId);
}