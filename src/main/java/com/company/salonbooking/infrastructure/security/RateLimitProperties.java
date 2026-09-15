package com.company.salonbooking.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@ConfigurationProperties(prefix = "app.rate-limit")
@Validated
public record RateLimitProperties(
        boolean enabled,
        DefaultLimit defaultLimit,
        Map<String, EndpointLimit> endpoints
) {
    public record DefaultLimit(
            int requestsPerMinute,
            int requestsPerSecond
    ) {}

    public record EndpointLimit(
            int requestsPerMinute,
            int requestsPerSecond
    ) {}
}