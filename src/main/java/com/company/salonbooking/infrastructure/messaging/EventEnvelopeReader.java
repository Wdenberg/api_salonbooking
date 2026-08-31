package com.company.salonbooking.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Reads just the envelope's routing fields (eventId, eventType) without deserializing the full payload type. */
@Component
public class EventEnvelopeReader {

    private final ObjectMapper objectMapper;

    public EventEnvelopeReader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public record EnvelopeHeader(UUID eventId, String eventType, UUID aggregateId, JsonNode payload) {}

    public EnvelopeHeader read(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            UUID eventId = UUID.fromString(root.get("eventId").asText());
            String eventType = root.get("eventType").asText();
            JsonNode payload = root.get("payload");
            UUID aggregateId = UUID.fromString(root.get("aggregateId").asText());
            return new EnvelopeHeader(eventId, eventType,aggregateId, payload);
        } catch (Exception e) {
            throw new IllegalArgumentException("Malformed event envelope", e);
        }
    }
}