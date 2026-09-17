package com.company.salonbooking.admin.interfaces.rest;

import com.company.salonbooking.AbstractIntegrationTest;
import com.company.salonbooking.identity.interfaces.rest.dto.LoginRequest;
import com.company.salonbooking.identity.interfaces.rest.dto.RegisterCustomerRequest;
import com.company.salonbooking.identity.interfaces.rest.dto.RegisterOwnerRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminEndpointIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JdbcTemplate jdbcTemplate;

    private static final String PASSWORD = "password123";
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        String adminEmail = "admin-" + UUID.randomUUID() + "@test.com";
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register/owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterOwnerRequest("Admin User", adminEmail, PASSWORD))))
                .andExpect(status().isCreated())
                .andReturn();

        String adminUserId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("userId").asText();

        jdbcTemplate.update("DELETE FROM user_roles WHERE user_id = ?", UUID.fromString(adminUserId));
        jdbcTemplate.update("INSERT INTO user_roles (user_id, role) VALUES (?, 'PLATFORM_ADMIN')",
                UUID.fromString(adminUserId));

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(adminEmail, PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();

        adminToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    @Test
    void devePermitirPlatFormAdminListarUsers() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void devePermitirPlatFormAdminListarBusinesses() throws Exception {
        mockMvc.perform(get("/api/v1/admin/businesses")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void devePermitirPlatFormAdminListarAppointments() throws Exception {
        mockMvc.perform(get("/api/v1/admin/appointments")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void ownerNaoDeveAcessarEndpointsAdmin() throws Exception {
        String ownerToken = registerOwnerAndGetToken("owner-admin-denied-" + UUID.randomUUID() + "@test.com");

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void customerNaoDeveAcessarEndpointsAdmin() throws Exception {
        String customerToken = registerCustomerAndGetToken("customer-admin-denied-" + UUID.randomUUID() + "@test.com");

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void endpointAdminSemAutenticacaoDeveRetornar401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    // --- Helpers ---

    private String registerOwnerAndGetToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register/owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterOwnerRequest("Owner", email, PASSWORD))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    private String registerCustomerAndGetToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterCustomerRequest("Cliente", email, PASSWORD))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken").asText();
    }
}
