package com.company.salonbooking.business.interfaces.rest;

import com.company.salonbooking.AbstractIntegrationTest;
import com.company.salonbooking.business.interfaces.rest.dto.CreateBusinessRequest;
import com.company.salonbooking.business.interfaces.rest.dto.UpdateBusinessRequest;
import com.company.salonbooking.identity.interfaces.rest.dto.RegisterOwnerRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BusinessControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String registerOwnerAndGetToken(String email) throws Exception {
        RegisterOwnerRequest request = new RegisterOwnerRequest("Owner", email, "password123");
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register/owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("accessToken").asText();
    }

    @Test
    void deveCriarELerBusiness() throws Exception {
        String token = registerOwnerAndGetToken("owner-biz1@example.com");

        CreateBusinessRequest createRequest = new CreateBusinessRequest(
                "Barbearia Central", "A melhor da cidade", "1199999999", "contato@central.com",
                null, "America/Recife");

        MvcResult createResult = mockMvc.perform(post("/api/v1/businesses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Barbearia Central"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn();

        String businessId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/v1/businesses/" + businessId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Barbearia Central"));

        // Default settings should exist automatically
        mockMvc.perform(get("/api/v1/businesses/" + businessId + "/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slotIntervalMinutes").value(30));
    }

    @Test
    void naoDeveDeixarOutroOwnerAtualizarBusinessDeTerceiro() throws Exception {
        String ownerAToken = registerOwnerAndGetToken("owner-a@example.com");
        String ownerBToken = registerOwnerAndGetToken("owner-b@example.com");

        CreateBusinessRequest createRequest = new CreateBusinessRequest(
                "Salão da Ana", null, null, null, null, "UTC");

        MvcResult createResult = mockMvc.perform(post("/api/v1/businesses")
                        .header("Authorization", "Bearer " + ownerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String businessId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        UpdateBusinessRequest updateRequest = new UpdateBusinessRequest("Nome Hackeado", null, null, null, null);

        // Owner B tenta atualizar o business do Owner A (IDOR - Seção 125)
        mockMvc.perform(put("/api/v1/businesses/" + businessId)
                        .header("Authorization", "Bearer " + ownerBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("RESOURCE_ACCESS_DENIED"));
    }

    @Test
    void clienteNaoPodeCriarBusiness() throws Exception {
        RegisterOwnerRequest customerLike = new RegisterOwnerRequest("Customer", "customer1@example.com", "password123");
        // Registrando via endpoint de customer para obter role CUSTOMER
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new com.company.salonbooking.identity.interfaces.rest.dto.RegisterCustomerRequest(
                                        "Customer", "customer1@example.com", "password123"))))
                .andExpect(status().isCreated())
                .andReturn();

        String token = objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();

        CreateBusinessRequest createRequest = new CreateBusinessRequest("Tentativa", null, null, null, null, "UTC");

        mockMvc.perform(post("/api/v1/businesses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }
}