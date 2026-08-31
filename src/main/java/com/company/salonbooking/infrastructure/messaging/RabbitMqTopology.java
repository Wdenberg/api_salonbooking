package com.company.salonbooking.infrastructure.messaging;


/**
 * Centraliza todos os nomes de exchange, fila e chave de roteamento (Seção 31). Mantidos como
 * constantes simples, em vez de literais de string dispersos, para garantir que as
 * configurações de publicador e consumidor nunca fiquem desalinhadas.
 */

public final class RabbitMqTopology {

    private RabbitMqTopology(){}

    // Exchanges
    public static final String APPOINTMENT_EVENTS_EXCHANGE = "appointment.events";
    public static final String NOTIFICATION_EVENTS_EXCHANGE = "notification.events";
    public static final String REPORT_EVENTS_EXCHANGE = "report.events";

    // Main queues
    public static final String APPOINTMENT_NOTIFICATION_QUEUE = "appointment.notification.queue";
    public static final String APPOINTMENT_REMINDER_QUEUE = "appointment.reminder.queue";
    public static final String REPORT_GENERATION_QUEUE = "report.generation.queue";

    // Retry queues (one per main queue, TTL set per-message on publish — Seção 32)
    public static final String APPOINTMENT_NOTIFICATION_RETRY_QUEUE = "appointment.notification.retry.queue";
    public static final String APPOINTMENT_REMINDER_RETRY_QUEUE = "appointment.reminder.retry.queue";
    public static final String REPORT_GENERATION_RETRY_QUEUE = "report.generation.retry.queue";

    // Retry exchanges (direct, message parks here with a TTL then dead-letters back to main queue)
    public static final String APPOINTMENT_NOTIFICATION_RETRY_EXCHANGE = "appointment.notification.retry.exchange";
    public static final String APPOINTMENT_REMINDER_RETRY_EXCHANGE = "appointment.reminder.retry.exchange";
    public static final String REPORT_GENERATION_RETRY_EXCHANGE = "report.generation.retry.exchange";

    // Final DLQs (Seção 31) — messages land here after exhausting all retry attempts
    public static final String APPOINTMENT_NOTIFICATION_DLQ = "appointment.notification.dlq";
    public static final String APPOINTMENT_REMINDER_DLQ = "appointment.reminder.dlq";
    public static final String REPORT_GENERATION_DLQ = "report.generation.dlq";

    // Routing keys — appointment.* covers created/confirmed/cancelled/completed (Seção 29 eventType)
    public static final String ROUTING_KEY_APPOINTMENT_ALL = "appointment.*";
    public static final String ROUTING_KEY_REPORT_REQUESTED = "report.requested";

    public static final String ROUTING_KEY_REMINDER_REQUESTED = "reminder.requested";

    public static String appointmentRoutingKey(String eventType) {
        // Se já começar com "appointment.", retorna diretamente (caso já esteja no formato final)
        if (eventType.startsWith("appointment.")) {
            return eventType;
        }
        // Caso contrário, remove prefixo "Appointment" e converte para dot.case
        String suffix = eventType.replaceFirst("^Appointment", "");
        return "appointment." + camelToDotLower(suffix);
    }

    private static String camelToDotLower(String value){
        return value.replaceAll("([a-z])([A-Z])", "$1.$2").toLowerCase();
    }
}
