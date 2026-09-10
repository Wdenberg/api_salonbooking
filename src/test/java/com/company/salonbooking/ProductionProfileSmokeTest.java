package com.company.salonbooking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Verifies the "prod" profile's property structure resolves correctly (ddl-auto=validate,
 * required env vars) without actually deploying — catches config typos (e.g. a renamed
 * property key) that would otherwise only surface during a real production rollout.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("prod")
@Testcontainers
@ContextConfiguration(initializers = ProductionProfileSmokeTest.Initializer.class)
class ProductionProfileSmokeTest {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18")
            .withDatabaseName("salonbooking_prod_smoke")
            .withUsername("salonbooking")
            .withPassword("salonbooking");

    static {
        POSTGRES.start();
    }

    @Test
    void contextDeveCarregarComProfileProd() {
        // If the Spring context fails to start (e.g. due to a malformed application-prod.yml
        // property path), this test fails at context-load time before any assertion runs.
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            TestPropertyValues.of(
                    "DATABASE_HOST=" + POSTGRES.getHost(),
                    "DATABASE_PORT=" + POSTGRES.getMappedPort(5432),
                    "DATABASE_NAME=salonbooking_prod_smoke",
                    "DATABASE_USERNAME=salonbooking",
                    "DATABASE_PASSWORD=salonbooking",
                    "RABBITMQ_HOST=localhost",
                    "RABBITMQ_PORT=5672",
                    "RABBITMQ_USERNAME=guest",
                    "RABBITMQ_PASSWORD=guest",
                    "JWT_SECRET=prod-smoke-test-secret-must-be-32-bytes-min",
                    "JWT_EXPIRATION_SECONDS=3600"
            ).applyTo(context.getEnvironment());
        }
    }
}
