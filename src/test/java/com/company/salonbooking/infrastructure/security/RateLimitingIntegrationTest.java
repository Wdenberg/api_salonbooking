package com.company.salonbooking.infrastructure.security;

import com.company.salonbooking.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RateLimitingIntegrationTest extends AbstractIntegrationTest {

    @DynamicPropertySource
    static void enableRateLimiting(DynamicPropertyRegistry registry) {
        registry.add("app.rate-limit.enabled", () -> true);
        registry.add("app.rate-limit.default-limit.requests-per-minute", () -> 3);
        registry.add("app.rate-limit.default-limit.requests-per-second", () -> 3);
    }

    @Autowired private MockMvc mockMvc;

    @Test
    void deveAplicarRateLimitCompleto() throws Exception {
        String body = "{\"email\":\"nao-existe@test.com\",\"password\":\"senha123\"}";

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("X-RateLimit-Limit-Minute"))
                .andExpect(header().exists("X-RateLimit-Remaining"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("RATE_LIMIT_EXCEEDED"))
                .andExpect(header().exists("Retry-After"))
                .andExpect(header().string("Retry-After", notNullValue()));

        Thread.sleep(61000);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isUnauthorized());
    }
}
