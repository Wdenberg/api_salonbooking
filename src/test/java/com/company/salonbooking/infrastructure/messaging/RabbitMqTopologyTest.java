package com.company.salonbooking.infrastructure.messaging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RabbitMqTopologyTest {

    @Test
    void deveConverterEventTypeParaRoutingKey() {
        assertThat(RabbitMqTopology.appointmentRoutingKey("AppointmentCreated")).isEqualTo("appointment.created");
        assertThat(RabbitMqTopology.appointmentRoutingKey("AppointmentConfirmed")).isEqualTo("appointment.confirmed");
        assertThat(RabbitMqTopology.appointmentRoutingKey("AppointmentCancelled")).isEqualTo("appointment.cancelled");
        assertThat(RabbitMqTopology.appointmentRoutingKey("AppointmentCompleted")).isEqualTo("appointment.completed");
    }
}