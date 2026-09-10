package com.company.salonbooking.catalog.interfaces.rest;

import com.company.salonbooking.AbstractIntegrationTest;
import com.company.salonbooking.business.interfaces.rest.dto.CreateBusinessRequest;
import com.company.salonbooking.catalog.interfaces.rest.dto.CreateServiceRequest;
import com.company.salonbooking.catalog.interfaces.rest.dto.UpdateServiceRequest;
import com.company.salonbooking.identity.interfaces.rest.dto.RegisterOwnerRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ServiceCacheIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CacheManager cacheManager;

    @Test
    void servicoDeveSerCacheadoAposPrimeiraLeitura() throws Exception {
        String ownerToken = registerOwnerAndGetToken("owner-cache1@example.com");
        String businessId = createBusiness(ownerToken);
        String serviceId = createService(ownerToken, businessId);

        mockMvc.perform(get("/api/v1/services/" + serviceId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk());

        var cache = cacheManager.getCache("catalog-services");
        assertThat(cache).isNotNull();
        assertThat(cache.get(java.util.UUID.fromString(serviceId))).isNotNull();
    }

    @Test
    void cacheDeveExpirarAposTTL() throws Exception {
        String ownerToken = registerOwnerAndGetToken("owner-cache2@example.com");
        String businessId = createBusiness(ownerToken);
        String serviceId = createService(ownerToken, businessId);

        mockMvc.perform(get("/api/v1/services/" + serviceId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk());

        var cache = cacheManager.getCache("catalog-services");
        assertThat(cache.get(java.util.UUID.fromString(serviceId))).isNotNull();

        // application-test.yml sets a 2-second TTL for this cache.
        await().atMost(5, SECONDS).untilAsserted(() ->
                assertThat(cache.get(java.util.UUID.fromString(serviceId))).isNull());
    }

    @Test
    void atualizarServicoDeveInvalidarCacheImediatamente() throws Exception {
        String ownerToken = registerOwnerAndGetToken("owner-cache3@example.com");
        String businessId = createBusiness(ownerToken);
        String serviceId = createService(ownerToken, businessId);

        mockMvc.perform(get("/api/v1/services/" + serviceId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk());
        assertThat(cacheManager.getCache("catalog-services").get(java.util.UUID.fromString(serviceId))).isNotNull();

        UpdateServiceRequest updateRequest = new UpdateServiceRequest("Corte Premium", null,
                new BigDecimal("60.00"), "BRL", 45);

        mockMvc.perform(put("/api/v1/services/" + serviceId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        // Evicted synchronously by @CacheEvict — no need to wait for TTL.
        assertThat(cacheManager.getCache("catalog-services").get(java.util.UUID.fromString(serviceId))).isNull();

        mockMvc.perform(get("/api/v1/services/" + serviceId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Corte Premium"));
    }

    // --- Helpers ---

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
                                new CreateBusinessRequest("Barbearia Cache", null, null, null, null, "UTC"))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private String createService(String ownerToken, String businessId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/businesses/" + businessId + "/services")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateServiceRequest("Corte", null, new BigDecimal("40.00"), "BRL", 30))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }
}
