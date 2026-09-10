package com.company.salonbooking.security;

import com.company.salonbooking.AbstractIntegrationTest;
import com.company.salonbooking.business.interfaces.rest.dto.CreateBusinessRequest;
import com.company.salonbooking.business.interfaces.rest.dto.OpeningHourIntervalDto;
import com.company.salonbooking.catalog.interfaces.rest.dto.CreateServiceRequest;
import com.company.salonbooking.employee.interfaces.rest.dto.CreateEmployeeRequest;
import com.company.salonbooking.employee.interfaces.rest.dto.EmployeeScheduleIntervalDto;
import com.company.salonbooking.identity.interfaces.rest.dto.RegisterCustomerRequest;
import com.company.salonbooking.identity.interfaces.rest.dto.RegisterOwnerRequest;
import com.company.salonbooking.scheduling.interfaces.rest.dto.CreateAppointmentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Consolidates every scenario listed in Seção 75 into one place, for traceability
 * against the prompt's explicit checklist. Most scenarios were already exercised in
 * earlier phases (referenced in comments below); this class adds the two that had no
 * dedicated coverage yet: "employee tentando cadastrar funcionário" and
 * "funcionário acessando outro business".
 */
class CrossTenantAccessSecurityTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    // --- "cliente acessando outro cliente" ---
    // Already covered: AppointmentSecurityIntegrationTest (Fase 6).

    // --- "cliente acessando dados de outro estabelecimento" ---
    // Already covered: BusinessControllerIntegrationTest.naoDeveDeixarOutroOwnerAtualizarBusinessDeTerceiro (Fase 3, owner-vs-owner variant)
    // and ReportControllerIntegrationTest.outroOwnerNaoDeveVisualizarRelatorioAlheio (Fase 11).

    // --- "owner acessando outro business" ---
    // Already covered: EmployeeControllerIntegrationTest.naoDeveDeixarOutroOwnerCriarFuncionarioEmBusinessAlheio (Fase 4)
    // and ServiceControllerIntegrationTest.naoDeveDeixarOutroOwnerDesativarServicoAlheio (Fase 5).

    @Test
    void employeeAcessandoOutroBusiness_naoDeveConseguirConfirmarAgendamentoDeOutroNegocio() throws Exception {
        // Business A with its own employee and appointment
        String ownerAToken = registerOwnerAndGetToken("owner-sec-a@example.com");
        String businessAId = createBusiness(ownerAToken, "Barbearia A");
        setOpeningHours(ownerAToken, businessAId);
        String employeeAId = createEmployee(ownerAToken, businessAId, "barbeiro-sec-a@example.com");
        setEmployeeSchedule(ownerAToken, employeeAId);
        String serviceAId = createService(ownerAToken, businessAId);
        String customerToken = registerCustomerAndGetToken("cliente-sec-x@example.com");

        Instant startAt = Instant.now().plus(3, ChronoUnit.DAYS).truncatedTo(ChronoUnit.HOURS).plus(10, ChronoUnit.HOURS);
        String appointmentId = createAppointment(customerToken, businessAId, employeeAId, serviceAId, startAt);

        // Business B, a completely unrelated employee
        String ownerBToken = registerOwnerAndGetToken("owner-sec-b@example.com");
        String businessBId = createBusiness(ownerBToken, "Barbearia B");
        String employeeBId = createEmployee(ownerBToken, businessBId, "barbeiro-sec-b@example.com");

        // Employee B (different business) must not be able to confirm Business A's appointment.
        // Authorization is enforced by comparing the appointment's employeeId/business ownership
        // against the requester — not by trusting any client-supplied businessId (Seção 124).
        String employeeBToken = loginAndGetToken("barbeiro-sec-b@example.com");

        mockMvc.perform(patch("/api/v1/appointments/" + appointmentId + "/confirm")
                        .header("Authorization", "Bearer " + employeeBToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void employeeNaoPodeCadastrarFuncionario() throws Exception {
        String ownerToken = registerOwnerAndGetToken("owner-sec-emp@example.com");
        String businessId = createBusiness(ownerToken, "Barbearia Emp");
        createEmployee(ownerToken, businessId, "barbeiro-sec-emp@example.com");

        String employeeToken = loginAndGetToken("barbeiro-sec-emp@example.com");

        CreateEmployeeRequest request = new CreateEmployeeRequest(
                "Outro Barbeiro", "outro-sec-emp@example.com", "password123", null);

        // ROLE_EMPLOYEE lacks ROLE_OWNER, so @PreAuthorize("hasRole('OWNER')") on
        // EmployeeController.create rejects this before the use case even runs.
        mockMvc.perform(post("/api/v1/businesses/" + businessId + "/employees")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // --- "cliente tentando criar serviço" ---
    // Already covered: ServiceControllerIntegrationTest.clienteNaoPodeCriarServico (Fase 5).

    // --- Helpers ---

    private String registerOwnerAndGetToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register/owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterOwnerRequest("Owner", email, "password123"))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private String registerCustomerAndGetToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterCustomerRequest("Cliente", email, "password123"))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private String loginAndGetToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new com.company.salonbooking.identity.interfaces.rest.dto.LoginRequest(email, "password123"))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private String createBusiness(String ownerToken, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/businesses")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateBusinessRequest(name, null, null, null, null, "UTC"))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private void setOpeningHours(String ownerToken, String businessId) throws Exception {
        List<OpeningHourIntervalDto> hours = List.of(
                new OpeningHourIntervalDto(DayOfWeek.MONDAY, LocalTime.of(0, 0), LocalTime.of(23, 59)),
                new OpeningHourIntervalDto(DayOfWeek.TUESDAY, LocalTime.of(0, 0), LocalTime.of(23, 59)),
                new OpeningHourIntervalDto(DayOfWeek.WEDNESDAY, LocalTime.of(0, 0), LocalTime.of(23, 59)),
                new OpeningHourIntervalDto(DayOfWeek.THURSDAY, LocalTime.of(0, 0), LocalTime.of(23, 59)),
                new OpeningHourIntervalDto(DayOfWeek.FRIDAY, LocalTime.of(0, 0), LocalTime.of(23, 59)),
                new OpeningHourIntervalDto(DayOfWeek.SATURDAY, LocalTime.of(0, 0), LocalTime.of(23, 59)),
                new OpeningHourIntervalDto(DayOfWeek.SUNDAY, LocalTime.of(0, 0), LocalTime.of(23, 59))
        );
        mockMvc.perform(put("/api/v1/businesses/" + businessId + "/opening-hours")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hours)))
                .andExpect(status().isOk());
    }

    private String createEmployee(String ownerToken, String businessId, String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/businesses/" + businessId + "/employees")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateEmployeeRequest("Barbeiro", email, "password123", "Corte"))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private void setEmployeeSchedule(String ownerToken, String employeeId) throws Exception {
        List<EmployeeScheduleIntervalDto> schedule = List.of(
                new EmployeeScheduleIntervalDto(DayOfWeek.MONDAY, LocalTime.of(0, 0), LocalTime.of(23, 59)),
                new EmployeeScheduleIntervalDto(DayOfWeek.TUESDAY, LocalTime.of(0, 0), LocalTime.of(23, 59)),
                new EmployeeScheduleIntervalDto(DayOfWeek.WEDNESDAY, LocalTime.of(0, 0), LocalTime.of(23, 59)),
                new EmployeeScheduleIntervalDto(DayOfWeek.THURSDAY, LocalTime.of(0, 0), LocalTime.of(23, 59)),
                new EmployeeScheduleIntervalDto(DayOfWeek.FRIDAY, LocalTime.of(0, 0), LocalTime.of(23, 59)),
                new EmployeeScheduleIntervalDto(DayOfWeek.SATURDAY, LocalTime.of(0, 0), LocalTime.of(23, 59)),
                new EmployeeScheduleIntervalDto(DayOfWeek.SUNDAY, LocalTime.of(0, 0), LocalTime.of(23, 59))
        );
        mockMvc.perform(put("/api/v1/employees/" + employeeId + "/schedule")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(schedule)))
                .andExpect(status().isOk());
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

    private String createAppointment(String customerToken, String businessId, String employeeId, String serviceId,
                                     Instant startAt) throws Exception {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                UUID.fromString(businessId), UUID.fromString(employeeId), UUID.fromString(serviceId), startAt, null);

        MvcResult result = mockMvc.perform(post("/api/v1/appointments")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }
}