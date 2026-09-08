package com.company.salonbooking.audit.domain.model;

/** Exhaustive list from Seção 41. Not every action necessarily has a hook yet —
 * new hooks can be added incrementally without touching this enum. */
public enum AuditAction {
    LOGIN,
    CREATE_BUSINESS,
    UPDATE_BUSINESS,
    CREATE_EMPLOYEE,
    DELETE_EMPLOYEE,
    CREATE_SERVICE,
    UPDATE_SERVICE,
    CREATE_APPOINTMENT,
    CANCEL_APPOINTMENT,
    COMPLETE_APPOINTMENT,
    GENERATE_REPORT
}