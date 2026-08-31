package com.company.salonbooking.infrastructure.messaging;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static com.company.salonbooking.infrastructure.messaging.RabbitMqTopology.*;

/**
 * Declara exchanges, filas, bindings e a cadeia de retry/DLQ (Seções 31 e 32).
 *
 * Padrão de retry por fila: a DLX da fila principal aponta para uma exchange de retry;
 * a fila de retry não possui consumidores nem TTL fixo — em vez disso, o consumidor
 * define uma propriedade de "expiração" por mensagem ao republicar uma mensagem que
 * falhou (veja RetryingMessageProcessor), permitindo que cada tentativa utilize um
 * intervalo de espera (backoff) diferente (5s, 30s, 5min... — Seção 32) sem a necessidade
 * de uma fila para cada nível de atraso. Quando o TTL da mensagem expira, a própria DLX
 * da fila de retry envia a mensagem de volta à fila principal para nova entrega. Após
 * atingir o número máximo de tentativas configurado, o consumidor encaminha a mensagem
 * para a DLQ final, em vez de tentar novamente.
 */
@Configuration
public class RabbitMqTopologyConfig {

    // --- Exchanges ---

    @Bean
    public TopicExchange appointmentEventsExchange() {
        return ExchangeBuilder.topicExchange(APPOINTMENT_EVENTS_EXCHANGE).durable(true).build();
    }

    @Bean
    public TopicExchange notificationEventsExchange() {
        return ExchangeBuilder.topicExchange(NOTIFICATION_EVENTS_EXCHANGE).durable(true).build();
    }

    @Bean
    public TopicExchange reportEventsExchange() {
        return ExchangeBuilder.topicExchange(REPORT_EVENTS_EXCHANGE).durable(true).build();
    }

    @Bean
    public DirectExchange appointmentNotificationRetryExchange() {
        return ExchangeBuilder.directExchange(APPOINTMENT_NOTIFICATION_RETRY_EXCHANGE).durable(true).build();
    }

    @Bean
    public DirectExchange appointmentReminderRetryExchange() {
        return ExchangeBuilder.directExchange(APPOINTMENT_REMINDER_RETRY_EXCHANGE).durable(true).build();
    }

    @Bean
    public DirectExchange reportGenerationRetryExchange() {
        return ExchangeBuilder.directExchange(REPORT_GENERATION_RETRY_EXCHANGE).durable(true).build();
    }

    // --- appointment.notification.queue chain ---

    @Bean
    public Queue appointmentNotificationQueue() {
        return QueueBuilder.durable(APPOINTMENT_NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", APPOINTMENT_NOTIFICATION_RETRY_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", APPOINTMENT_NOTIFICATION_RETRY_QUEUE)
                .build();
    }

    @Bean
    public Queue appointmentNotificationRetryQueue() {
        return QueueBuilder.durable(APPOINTMENT_NOTIFICATION_RETRY_QUEUE)
                .withArgument("x-dead-letter-exchange", "") // default exchange
                .withArgument("x-dead-letter-routing-key", APPOINTMENT_NOTIFICATION_QUEUE)
                .build();
    }

    @Bean
    public Queue appointmentNotificationDlq() {
        return QueueBuilder.durable(APPOINTMENT_NOTIFICATION_DLQ).build();
    }

    @Bean
    public Binding appointmentNotificationBinding() {
        return BindingBuilder.bind(appointmentNotificationQueue())
                .to(appointmentEventsExchange()).with(ROUTING_KEY_APPOINTMENT_ALL);
    }

    @Bean
    public Binding appointmentNotificationRetryBinding() {
        return BindingBuilder.bind(appointmentNotificationRetryQueue())
                .to(appointmentNotificationRetryExchange()).with(APPOINTMENT_NOTIFICATION_RETRY_QUEUE);
    }

    // --- appointment.reminder.queue chain ---

    @Bean
    public Queue appointmentReminderQueue() {
        return QueueBuilder.durable(APPOINTMENT_REMINDER_QUEUE)
                .withArgument("x-dead-letter-exchange", APPOINTMENT_REMINDER_RETRY_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", APPOINTMENT_REMINDER_RETRY_QUEUE)
                .build();
    }

    @Bean
    public Queue appointmentReminderRetryQueue() {
        return QueueBuilder.durable(APPOINTMENT_REMINDER_RETRY_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", APPOINTMENT_REMINDER_QUEUE)
                .build();
    }

    @Bean
    public Queue appointmentReminderDlq() {
        return QueueBuilder.durable(APPOINTMENT_REMINDER_DLQ).build();
    }

    @Bean
    public Binding appointmentReminderRetryBinding() {
        return BindingBuilder.bind(appointmentReminderRetryQueue())
                .to(appointmentReminderRetryExchange()).with(APPOINTMENT_REMINDER_RETRY_QUEUE);
    }
    // Nota: a vinculação da fila appointment.reminder.queue a notification.events com a
    // chave de roteamento "reminder.requested" é declarada na Fase 10, assim que os lembretes existirem.

    // --- cadeia da fila report.generation.queue ---

    @Bean
    public Queue reportGenerationQueue() {
        return QueueBuilder.durable(REPORT_GENERATION_QUEUE)
                .withArgument("x-dead-letter-exchange", REPORT_GENERATION_RETRY_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", REPORT_GENERATION_RETRY_QUEUE)
                .build();
    }

    @Bean
    public Queue reportGenerationRetryQueue() {
        return QueueBuilder.durable(REPORT_GENERATION_RETRY_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", REPORT_GENERATION_QUEUE)
                .build();
    }

    @Bean
    public Queue reportGenerationDlq() {
        return QueueBuilder.durable(REPORT_GENERATION_DLQ).build();
    }

    @Bean
    public Binding reportGenerationBinding() {
        return BindingBuilder.bind(reportGenerationQueue())
                .to(reportEventsExchange()).with(ROUTING_KEY_REPORT_REQUESTED);
    }

    @Bean
    public Binding reportGenerationRetryBinding() {
        return BindingBuilder.bind(reportGenerationRetryQueue())
                .to(reportGenerationRetryExchange()).with(REPORT_GENERATION_RETRY_QUEUE);
    }

    @Bean
    public Binding appointmentReminderBinding() {
        return BindingBuilder.bind(appointmentReminderQueue())
                .to(notificationEventsExchange()).with(ROUTING_KEY_REMINDER_REQUESTED);
    }
}