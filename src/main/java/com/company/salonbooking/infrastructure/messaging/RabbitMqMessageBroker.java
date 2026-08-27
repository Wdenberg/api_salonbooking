package com.company.salonbooking.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static com.company.salonbooking.infrastructure.messaging.RabbitMqTopology.APPOINTMENT_EVENTS_EXCHANGE;
import static com.company.salonbooking.infrastructure.messaging.RabbitMqTopology.appointmentRoutingKey;

@Component
public class RabbitMqMessageBroker implements MessageBroker {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public RabbitMqMessageBroker(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(String eventType, UUID eventId, UUID aggregateId, String payloadJson) {
        if (!eventType.startsWith("Appointment")) {
            throw new IllegalArgumentException("No routing configured for event type: " + eventType);
        }

        try {
            Map<String, Object> envelope = new LinkedHashMap<>();
            envelope.put("eventId", eventId.toString());
            envelope.put("eventType", eventType);
            JsonNode payloadNode = objectMapper.readTree(payloadJson);
            envelope.put("payload", payloadNode);

            String envelopeJson = objectMapper.writeValueAsString(envelope);

            String routingKey = appointmentRoutingKey(eventType);
            MessageProperties properties = new MessageProperties();
            properties.setContentType("application/json");
            properties.setHeader("eventType", eventType);
            properties.setHeader("aggregateId", aggregateId.toString());
            rabbitTemplate.send(APPOINTMENT_EVENTS_EXCHANGE, routingKey,
                    new Message(envelopeJson.getBytes(StandardCharsets.UTF_8), properties));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize envelope", e);
        }
    }
}