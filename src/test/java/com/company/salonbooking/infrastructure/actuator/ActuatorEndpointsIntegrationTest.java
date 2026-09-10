package com.company.salonbooking.infrastructure.actuator;

import com.company.salonbooking.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ActuatorEndpointsIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void healthEndpointDeveEstarPublicoEDeveResponderUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void livenessEReadinessDevemEstarDisponiveis() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void metricsEndpointDeveExporMetricasCustomizadasAposUsoDaApi() throws Exception {
        mockMvc.perform(get("/actuator/metrics/appointments.created"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("appointments.created"));
    }

    @Test
    void prometheusEndpointDeveResponderComFormatoTextual() throws Exception {
        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk());
    }
}
