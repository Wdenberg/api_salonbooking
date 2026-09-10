package com.company.salonbooking.infrastructure.openapi;

import com.company.salonbooking.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Verifies Seção 76's OpenAPI/Swagger requirement is actually served, not just configured. */
class OpenApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void apiDocsDeveResponderComEsquemaValido() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Salon Booking API"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"));
    }

    @Test
    void swaggerUiDeveEstarAcessivel() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection()); // redirects to swagger-ui/index.html
    }
}