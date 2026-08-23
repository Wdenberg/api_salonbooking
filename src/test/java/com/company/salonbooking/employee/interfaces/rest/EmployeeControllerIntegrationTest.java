package com.company.salonbooking.employee.interfaces.rest;

import com.company.salonbooking.AbstractIntegrationTest;
import com.company.salonbooking.business.interfaces.rest.dto.CreateBusinessRequest;
import com.company.salonbooking.employee.interfaces.rest.dto.CreateEmployeeRequest;
import com.company.salonbooking.identity.interfaces.rest.dto.RegisterOwnerRequest;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class EmployeeControllerIntegrationTest extends AbstractIntegrationTest {

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
    void deveCriarFuncionarioEBuscarPorId() throws Exception {
        String ownerToken = registerOwnerAndGetToken("owner-emp1@example.com");
        String businessId = createBusiness(ownerToken);

        CreateEmployeeRequest request = new CreateEmployeeRequest("Barbeiro Zé", "barbeiro1@example.com", "password123", "Corte masculino");

        MvcResult result = mockMvc.perform(post("/api/v1/businesses/" + businessId + "/employees")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn();

        String employeeId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/v1/employees/" + employeeId).header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialty").value("Corte masculino"));
    }

    @Test
    void naoDeveDeixarOutroOwnerCriarFuncionarioEmBusinessAlheio() throws Exception {
        String ownerAToken = registerOwnerAndGetToken("owner-emp-a@example.com");
        String ownerBToken = registerOwnerAndGetToken("owner-emp-b@example.com");
        String businessId = createBusiness(ownerAToken);

        CreateEmployeeRequest request = new CreateEmployeeRequest("Invasor", "invasor@example.com", "password123", null);

        mockMvc.perform(post("/api/v1/businesses/" + businessId + "/employees")
                        .header("Authorization", "Bearer " + ownerBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}