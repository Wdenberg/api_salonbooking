package com.company.salonbooking;

import com.company.salonbooking.infrastructure.outbox.MutableClock;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

@TestConfiguration
public class TestClockConfig {

    @Bean
    @Primary
    public Clock testClock() {
        return new MutableClock(Instant.now(), ZoneId.systemDefault());
    }
}