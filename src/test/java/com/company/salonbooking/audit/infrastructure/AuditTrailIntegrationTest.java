package com.company.salonbooking.audit.infrastructure;

import com.company.salonbooking.AbstractIntegrationTest;
import com.company.salonbooking.audit.domain.model.AuditAction;
import com.company.salonbooking.audit.infrastructure.persistence.AuditEventJpaRepository;
import com.company.salonbooking.business.interfaces.rest.dto.CreateBusinessRequest;
import com.company.salonbooking.identity.interfaces.rest.dto.RegisterOwnerRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuditTrailIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private AuditEventJpaRepository auditEventJpaRepository;

    @Test
    void registroEcriacaoDeBusinessDevemGerarEventosDeAuditoria() throws Exception {
        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register/owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterOwnerRequest("Owner", "owner-audit1@example.com", "password123"))))
                .andExpect(status().isCreated())
                .andReturn();

        var json = objectMapper.readTree(registerResult.getResponse().getContentAsString());
        String token = json.get("accessToken").asText();
        UUID ownerId = UUID.fromString(json.get("userId").asText());

        mockMvc.perform(post("/api/v1/businesses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateBusinessRequest("Barbearia Audit", null, null, null, null, "UTC"))))
                .andExpect(status().isCreated());

        var allEvents = auditEventJpaRepository.findAll();
        List<AuditAction> actions = allEvents.stream()
                .filter(e -> e.getActorUserId().equals(ownerId))
                .map(e -> e.getAction())
                .toList();

        // LOGIN is not recorded on register/owner (Seção 41 lists LOGIN as its own
        // action, distinct from account creation) — but CREATE_BUSINESS must be present.
        assertThat(actions).contains(AuditAction.CREATE_BUSINESS);
    }

    @Test
    void loginBemSucedidoDeveGerarEventoDeAuditoria() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register/owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterOwnerRequest("Owner", "owner-audit2@example.com", "password123"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new com.company.salonbooking.identity.interfaces.rest.dto.LoginRequest(
                                        "owner-audit2@example.com", "password123"))))
                .andExpect(status().isOk());

        var loginEvents = auditEventJpaRepository.findAll().stream()
                .filter(e -> e.getAction() == AuditAction.LOGIN)
                .toList();

        assertThat(loginEvents).isNotEmpty();
    }
}
