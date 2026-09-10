package com.company.salonbooking.infrastructure.web;

import com.company.salonbooking.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CorrelationIdIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void deveGerarCorrelationIdQuandoNaoFornecido() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-Id"));
    }

    @Test
    void deveEcoarCorrelationIdFornecidoPeloCliente() throws Exception {
        String customId = UUID.randomUUID().toString();

        mockMvc.perform(get("/actuator/health").header("X-Correlation-Id", customId))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id", customId));
    }
}
