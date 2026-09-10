package com.company.salonbooking.employee.interfaces.rest;

import com.company.salonbooking.AbstractIntegrationTest;
import com.company.salonbooking.identity.interfaces.rest.dto.RegisterCustomerRequest;
import com.company.salonbooking.employee.interfaces.rest.dto.CreateEmployeeRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Closes the last Seção 75 gap: "cliente tentando cadastrar funcionário" as a dedicated HTTP test. */
class EmployeeAuthorizationSecurityTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void clienteNaoPodeCadastrarFuncionario() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterCustomerRequest("Cliente", "cliente-sec-emp@example.com", "password123"))))
                .andExpect(status().isCreated())
                .andReturn();
        String customerToken = objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();

        // A random businessId is enough — @PreAuthorize("hasRole('OWNER')") rejects
        // this before the request ever reaches CreateEmployeeUseCase / touches the DB.
        CreateEmployeeRequest request = new CreateEmployeeRequest("Tentativa", "tentativa@example.com", "password123", null);

        mockMvc.perform(post("/api/v1/businesses/" + UUID.randomUUID() + "/employees")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
