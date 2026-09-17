package com.company.salonbooking.business.interfaces.rest;

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

class DeleteEndpointsIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    // --- Business DELETE ---

    @Test
    void deveDeletarBusinessDoOwner() throws Exception {
        String ownerToken = registerOwnerAndGetToken("owner-del-biz1@example.com");
        String businessId = createBusiness(ownerToken, "Barbearia Del 1");

        mockMvc.perform(delete("/api/v1/businesses/" + businessId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void ownerNaoDeveDeletarBusinessDeTerceiro() throws Exception {
        String ownerAToken = registerOwnerAndGetToken("owner-del-biz-a@example.com");
        String businessAId = createBusiness(ownerAToken, "Barbearia Del A");

        String ownerBToken = registerOwnerAndGetToken("owner-del-biz-b@example.com");

        mockMvc.perform(delete("/api/v1/businesses/" + businessAId)
                        .header("Authorization", "Bearer " + ownerBToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void naoDeveDeletarBusinessComAgendamentosAtivos() throws Exception {
        String ownerToken = registerOwnerAndGetToken("owner-del-biz2@example.com");
        String businessId = createBusiness(ownerToken, "Barbearia Del 2");
        setOpeningHours(ownerToken, businessId);
        String employeeId = createEmployee(ownerToken, businessId, "barbeiro-del-biz2@example.com");
        setEmployeeSchedule(ownerToken, employeeId);
        String serviceId = createService(ownerToken, businessId);
        String customerToken = registerCustomerAndGetToken("cliente-del-biz2@example.com");

        Instant startAt = Instant.now().plus(3, ChronoUnit.DAYS).truncatedTo(ChronoUnit.HOURS).plus(10, ChronoUnit.HOURS);
        createAppointment(customerToken, businessId, employeeId, serviceId, startAt);

        mockMvc.perform(delete("/api/v1/businesses/" + businessId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isConflict());
    }

    // --- Service DELETE ---

    @Test
    void deveDeletarServicoDoOwner() throws Exception {
        String ownerToken = registerOwnerAndGetToken("owner-del-svc1@example.com");
        String businessId = createBusiness(ownerToken, "Barbearia Del Svc 1");
        String serviceId = createService(ownerToken, businessId);

        mockMvc.perform(delete("/api/v1/services/" + serviceId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void ownerNaoDeveDeletarServicoDeTerceiro() throws Exception {
        String ownerAToken = registerOwnerAndGetToken("owner-del-svc-a@example.com");
        String businessAId = createBusiness(ownerAToken, "Barbearia Del Svc A");
        String serviceAId = createService(ownerAToken, businessAId);

        String ownerBToken = registerOwnerAndGetToken("owner-del-svc-b@example.com");

        mockMvc.perform(delete("/api/v1/services/" + serviceAId)
                        .header("Authorization", "Bearer " + ownerBToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void naoDeveDeletarServicoComAgendamentosAtivos() throws Exception {
        String ownerToken = registerOwnerAndGetToken("owner-del-svc2@example.com");
        String businessId = createBusiness(ownerToken, "Barbearia Del Svc 2");
        setOpeningHours(ownerToken, businessId);
        String employeeId = createEmployee(ownerToken, businessId, "barbeiro-del-svc2@example.com");
        setEmployeeSchedule(ownerToken, employeeId);
        String serviceId = createService(ownerToken, businessId);
        String customerToken = registerCustomerAndGetToken("cliente-del-svc2@example.com");

        Instant startAt = Instant.now().plus(3, ChronoUnit.DAYS).truncatedTo(ChronoUnit.HOURS).plus(10, ChronoUnit.HOURS);
        createAppointment(customerToken, businessId, employeeId, serviceId, startAt);

        mockMvc.perform(delete("/api/v1/services/" + serviceId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isConflict());
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

    private String registerCustomerAndGetToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterCustomerRequest("Cliente", email, "password123"))))
                .andExpect(status().isCreated())
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
