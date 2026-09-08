package com.company.salonbooking.audit.interfaces.rest;

import com.company.salonbooking.AbstractIntegrationTest;
import com.company.salonbooking.business.interfaces.rest.dto.CreateBusinessRequest;
import com.company.salonbooking.identity.interfaces.rest.dto.RegisterOwnerRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuditControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void ownerDeveVerSeuPropioAuditLog() throws Exception {
        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register/owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterOwnerRequest("Owner", "owner-audit3@example.com", "password123"))))
                .andExpect(status().isCreated())
                .andReturn();
        String token = objectMapper.readTree(registerResult.getResponse().getContentAsString()).get("accessToken").asText();

        MvcResult businessResult = mockMvc.perform(post("/api/v1/businesses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateBusinessRequest("Barbearia Audit Log", null, null, null, null, "UTC"))))
                .andExpect(status().isCreated())
                .andReturn();
        String businessId = objectMapper.readTree(businessResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/v1/businesses/" + businessId + "/audit-events")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("CREATE_BUSINESS"));
    }

    @Test
    void outroOwnerNaoDeveVerAuditLogAlheio() throws Exception {
        MvcResult ownerAResult = mockMvc.perform(post("/api/v1/auth/register/owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterOwnerRequest("Owner A", "owner-audit-a@example.com", "password123"))))
                .andExpect(status().isCreated())
                .andReturn();
        String tokenA = objectMapper.readTree(ownerAResult.getResponse().getContentAsString()).get("accessToken").asText();

        MvcResult businessResult = mockMvc.perform(post("/api/v1/businesses")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateBusinessRequest("Barbearia Privada", null, null, null, null, "UTC"))))
                .andExpect(status().isCreated())
                .andReturn();
        String businessId = objectMapper.readTree(businessResult.getResponse().getContentAsString()).get("id").asText();

        MvcResult ownerBResult = mockMvc.perform(post("/api/v1/auth/register/owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterOwnerRequest("Owner B", "owner-audit-b@example.com", "password123"))))
                .andExpect(status().isCreated())
                .andReturn();
        String tokenB = objectMapper.readTree(ownerBResult.getResponse().getContentAsString()).get("accessToken").asText();

        mockMvc.perform(get("/api/v1/businesses/" + businessId + "/audit-events")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());
    }
}