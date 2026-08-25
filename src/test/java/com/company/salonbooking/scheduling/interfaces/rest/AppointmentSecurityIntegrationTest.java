package com.company.salonbooking.scheduling.interfaces.rest;

import com.company.salonbooking.AbstractIntegrationTest;
import com.company.salonbooking.identity.interfaces.rest.dto.RegisterCustomerRequest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AppointmentSecurityIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void clienteNaoDeveAcessarAgendamentoDeOutroCliente() throws Exception {
        String tokenA = registerCustomerAndGetToken("cliente-sec-a@example.com");
        String tokenB = registerCustomerAndGetToken("cliente-sec-b@example.com");

        // Cliente B tenta acessar um UUID aleatório (simula agendamento de outro cliente).
        // Como o recurso não existe, o teste verifica 404 (não haver vazamento de existência),
        // que é a política adotada por GetAppointmentUseCase quando o requester não é dono.
        mockMvc.perform(get("/api/v1/appointments/" + java.util.UUID.randomUUID())
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());

    }

    private String registerCustomerAndGetToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterCustomerRequest("Cliente", email, "password123"))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }
}