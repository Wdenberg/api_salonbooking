package com.company.salonbooking.catalog.interfaces.rest;

import com.company.salonbooking.AbstractIntegrationTest;
import com.company.salonbooking.business.interfaces.rest.dto.CreateBusinessRequest;
import com.company.salonbooking.catalog.interfaces.rest.dto.CreateServiceRequest;
import com.company.salonbooking.identity.interfaces.rest.dto.RegisterOwnerRequest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ServiceControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String registerOwnerAndGetToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register/owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterOwnerRequest("Owner", email, "password123"))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private String createBusiness(String ownerToken) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/businesses")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateBusinessRequest("Barbearia Teste", null, null, null, null, "UTC"))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    @Test
    void deveCriarListarEBuscarServico() throws Exception {
        String ownerToken = registerOwnerAndGetToken("owner-cat1@example.com");
        String businessId = createBusiness(ownerToken);

        CreateServiceRequest request = new CreateServiceRequest("Corte Masculino", "Tesoura e máquina",
                new BigDecimal("45.00"), "BRL", 30);

        MvcResult createResult = mockMvc.perform(post("/api/v1/businesses/" + businessId + "/services")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.priceAmount").value(45.00))
                .andReturn();

        String serviceId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/v1/services/" + serviceId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.durationMinutes").value(30));

        mockMvc.perform(get("/api/v1/businesses/" + businessId + "/services")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Corte Masculino"));
    }

    @Test
    void naoDeveDeixarOutroOwnerDesativarServicoAlheio() throws Exception {
        String ownerAToken = registerOwnerAndGetToken("owner-cat-a@example.com");
        String ownerBToken = registerOwnerAndGetToken("owner-cat-b@example.com");
        String businessId = createBusiness(ownerAToken);

        CreateServiceRequest request = new CreateServiceRequest("Barba", null, new BigDecimal("25.00"), "BRL", 20);

        MvcResult createResult = mockMvc.perform(post("/api/v1/businesses/" + businessId + "/services")
                        .header("Authorization", "Bearer " + ownerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String serviceId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(patch("/api/v1/services/" + serviceId + "/status")
                        .header("Authorization", "Bearer " + ownerBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\": false}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("RESOURCE_ACCESS_DENIED"));
    }

    @Test
    void clienteNaoPodeCriarServico() throws Exception {
        MvcResult customerResult = mockMvc.perform(post("/api/v1/auth/register/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new com.company.salonbooking.identity.interfaces.rest.dto.RegisterCustomerRequest(
                                        "Cliente", "cliente-cat1@example.com", "password123"))))
                .andExpect(status().isCreated())
                .andReturn();
        String customerToken = objectMapper.readTree(customerResult.getResponse().getContentAsString()).get("accessToken").asText();

        String ownerToken = registerOwnerAndGetToken("owner-cat-c@example.com");
        String businessId = createBusiness(ownerToken);

        CreateServiceRequest request = new CreateServiceRequest("Tentativa", null, new BigDecimal("10.00"), "BRL", 10);

        mockMvc.perform(post("/api/v1/businesses/" + businessId + "/services")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}