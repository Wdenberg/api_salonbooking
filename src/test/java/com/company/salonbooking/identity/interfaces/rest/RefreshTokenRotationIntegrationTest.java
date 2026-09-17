package com.company.salonbooking.identity.interfaces.rest;

import com.company.salonbooking.AbstractIntegrationTest;
import com.company.salonbooking.identity.interfaces.rest.dto.LoginRequest;
import com.company.salonbooking.identity.interfaces.rest.dto.RefreshTokenRequest;
import com.company.salonbooking.identity.interfaces.rest.dto.RegisterOwnerRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RefreshTokenRotationIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static final String PASSWORD = "password123";

    @Test
    void deveRotacionarRefreshToken() throws Exception {
        String email = "refresh-rotate-" + UUID.randomUUID() + "@test.com";
        registerUser(email);
        String refreshToken = loginAndGetRefreshToken(email);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest(refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        String newRefreshToken = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("refreshToken").asText();

        org.junit.jupiter.api.Assertions.assertNotEquals(refreshToken, newRefreshToken);
    }

    @Test
    void deveRecusarRefreshTokenAntigoAposRotacao() throws Exception {
        String email = "refresh-old-" + UUID.randomUUID() + "@test.com";
        registerUser(email);
        String originalRefreshToken = loginAndGetRefreshToken(email);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest(originalRefreshToken))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest(originalRefreshToken))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveDetectarReusoDeRefreshTokenERevogarCadeia() throws Exception {
        String email = "refresh-reuse-" + UUID.randomUUID() + "@test.com";
        registerUser(email);
        String originalRefreshToken = loginAndGetRefreshToken(email);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest(originalRefreshToken))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest(originalRefreshToken))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest(originalRefreshToken))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRecusarRefreshTokenInvalido() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest("token-invalido-abc123"))))
                .andExpect(status().isUnauthorized());
    }

    private void registerUser(String email) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register/owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterOwnerRequest("Refresh Test", email, PASSWORD))))
                .andExpect(status().isCreated());
    }

    private String loginAndGetRefreshToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("refreshToken").asText();
    }
}
