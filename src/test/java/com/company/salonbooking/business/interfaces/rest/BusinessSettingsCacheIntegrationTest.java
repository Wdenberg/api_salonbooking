package com.company.salonbooking.business.interfaces.rest;

import com.company.salonbooking.AbstractIntegrationTest;
import com.company.salonbooking.business.interfaces.rest.dto.BusinessSettingsRequest;
import com.company.salonbooking.business.interfaces.rest.dto.CreateBusinessRequest;
import com.company.salonbooking.identity.interfaces.rest.dto.RegisterOwnerRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BusinessSettingsCacheIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CacheManager cacheManager;

    @Test
    void atualizarSettingsDeveInvalidarCache() throws Exception {
        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register/owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterOwnerRequest("Owner", "owner-settings-cache@example.com", "password123"))))
                .andExpect(status().isCreated())
                .andReturn();
        String token = objectMapper.readTree(registerResult.getResponse().getContentAsString()).get("accessToken").asText();

        MvcResult businessResult = mockMvc.perform(post("/api/v1/businesses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateBusinessRequest("Barbearia Settings", null, null, null, null, "UTC"))))
                .andExpect(status().isCreated())
                .andReturn();
        String businessId = objectMapper.readTree(businessResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/v1/businesses/" + businessId + "/settings")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        assertThat(cacheManager.getCache("business-settings").get(java.util.UUID.fromString(businessId))).isNotNull();

        BusinessSettingsRequest updateRequest = new BusinessSettingsRequest(90, 45, 180, 15);
        mockMvc.perform(put("/api/v1/businesses/" + businessId + "/settings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        assertThat(cacheManager.getCache("business-settings").get(java.util.UUID.fromString(businessId))).isNull();
    }
}
