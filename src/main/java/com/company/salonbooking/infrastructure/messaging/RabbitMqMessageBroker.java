package com.company.salonbooking.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static com.company.salonbooking.infrastructure.messaging.RabbitMqTopology.*;

@Component
public class RabbitMqMessageBroker implements MessageBroker {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public RabbitMqMessageBroker(
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(
            String eventType,
            UUID eventId,
            UUID aggregateId,
            String payloadJson
    ) {
        String exchange;
        String routingKey;

        if (eventType.equals("AppointmentReminderRequested")) {

            exchange = NOTIFICATION_EVENTS_EXCHANGE;
            routingKey = ROUTING_KEY_REMINDER_REQUESTED;

        } else if (eventType.startsWith("Appointment")) {

            exchange = APPOINTMENT_EVENTS_EXCHANGE;
            routingKey = appointmentRoutingKey(eventType);

        } else {

            throw new IllegalArgumentException(
                    "No routing configured for event type: " + eventType
            );
        }

        try {
            Map<String, Object> envelope = new LinkedHashMap<>();

            envelope.put("eventId", eventId.toString());
            envelope.put("eventType", eventType);
            envelope.put("aggregateId", aggregateId.toString());

            JsonNode payloadNode = objectMapper.readTree(payloadJson);
            envelope.put("payload", payloadNode);

            String envelopeJson =
                    objectMapper.writeValueAsString(envelope);

            MessageProperties properties = new MessageProperties();

            properties.setContentType("application/json");

            properties.setHeader(
                    "eventId",
                    eventId.toString()
            );

            properties.setHeader(
                    "eventType",
                    eventType
            );

            properties.setHeader(
                    "aggregateId",
                    aggregateId.toString()
            );

            rabbitTemplate.send(
                    exchange,
                    routingKey,
                    new Message(
                            envelopeJson.getBytes(StandardCharsets.UTF_8),
                            properties
                    )
            );

        } catch (JsonProcessingException e) {
            throw new RuntimeException(
                    "Failed to serialize envelope",
                    e
            );
        }
    }
}