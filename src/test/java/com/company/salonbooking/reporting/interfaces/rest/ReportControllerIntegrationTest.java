package com.company.salonbooking.reporting.interfaces.rest;

import com.company.salonbooking.AbstractIntegrationTest;
import com.company.salonbooking.business.interfaces.rest.dto.CreateBusinessRequest;
import com.company.salonbooking.business.interfaces.rest.dto.OpeningHourIntervalDto;
import com.company.salonbooking.catalog.interfaces.rest.dto.CreateServiceRequest;
import com.company.salonbooking.employee.interfaces.rest.dto.CreateEmployeeRequest;
import com.company.salonbooking.employee.interfaces.rest.dto.EmployeeScheduleIntervalDto;
import com.company.salonbooking.identity.interfaces.rest.dto.RegisterCustomerRequest;
import com.company.salonbooking.identity.interfaces.rest.dto.RegisterOwnerRequest;
import com.company.salonbooking.infrastructure.outbox.OutboxPublisherJob;
import com.company.salonbooking.reporting.application.usecase.ProcessReportUseCase;
import com.company.salonbooking.reporting.domain.model.ReportType;
import com.company.salonbooking.reporting.interfaces.rest.dto.GenerateReportRequest;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReportControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private OutboxPublisherJob outboxPublisherJob;
    @Autowired private ProcessReportUseCase processReportUseCase;

    @Test
    void deveGerarRelatorioDeReceitaAposAgendamentoCompletado() throws Exception {
        String ownerToken = registerOwnerAndGetToken("owner-rep1@example.com");
        String businessId = createBusiness(ownerToken);
        setOpeningHours(ownerToken, businessId);
        String employeeId = createEmployee(ownerToken, businessId, "barbeiro-rep1@example.com");
        setEmployeeSchedule(ownerToken, employeeId);
        String serviceId = createService(ownerToken, businessId);
        String customerToken = registerCustomerAndGetToken("cliente-rep1@example.com");

        Instant startAt = Instant.now().plus(3, ChronoUnit.DAYS).truncatedTo(ChronoUnit.HOURS).plus(10, ChronoUnit.HOURS);
        String appointmentId = createConfirmAndCompleteAppointment(ownerToken, customerToken, businessId, employeeId, serviceId, startAt);

        GenerateReportRequest reportRequest = new GenerateReportRequest(UUID.fromString(businessId), ReportType.REVENUE,
                LocalDate.now(ZoneOffset.UTC), LocalDate.now(ZoneOffset.UTC).plusDays(5));

        MvcResult createResult = mockMvc.perform(post("/api/v1/reports")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reportRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        String reportId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        // Process synchronously in the test instead of waiting on the async RabbitMQ round-trip.
        processReportUseCase.execute(UUID.fromString(reportId));

        mockMvc.perform(get("/api/v1/reports/" + reportId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.result.reportType").value("REVENUE"))
                .andExpect(jsonPath("$.result.completedAppointments").value(1))
                .andExpect(jsonPath("$.result.totalRevenue").value(40.00));
    }

    @Test
    void outroOwnerNaoDeveVisualizarRelatorioAlheio() throws Exception {
        String ownerAToken = registerOwnerAndGetToken("owner-rep-a@example.com");
        String ownerBToken = registerOwnerAndGetToken("owner-rep-b@example.com");
        String businessId = createBusiness(ownerAToken);

        GenerateReportRequest reportRequest = new GenerateReportRequest(UUID.fromString(businessId), ReportType.CANCELLATIONS,
                LocalDate.now(ZoneOffset.UTC), LocalDate.now(ZoneOffset.UTC).plusDays(5));

        MvcResult createResult = mockMvc.perform(post("/api/v1/reports")
                        .header("Authorization", "Bearer " + ownerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reportRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String reportId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/v1/reports/" + reportId)
                        .header("Authorization", "Bearer " + ownerBToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void clienteNaoPodeSolicitarRelatorio() throws Exception {
        MvcResult customerResult = mockMvc.perform(post("/api/v1/auth/register/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterCustomerRequest("Cliente", "cliente-rep2@example.com", "password123"))))
                .andExpect(status().isCreated())
                .andReturn();
        String customerToken = objectMapper.readTree(customerResult.getResponse().getContentAsString()).get("accessToken").asText();

        String ownerToken = registerOwnerAndGetToken("owner-rep-c@example.com");
        String businessId = createBusiness(ownerToken);

        GenerateReportRequest reportRequest = new GenerateReportRequest(UUID.fromString(businessId), ReportType.APPOINTMENTS,
                LocalDate.now(ZoneOffset.UTC), LocalDate.now(ZoneOffset.UTC).plusDays(1));

        mockMvc.perform(post("/api/v1/reports")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reportRequest)))
                .andExpect(status().isForbidden());
    }

    // --- Helpers ---

    private String createConfirmAndCompleteAppointment(String ownerToken, String customerToken, String businessId,
                                                       String employeeId, String serviceId, Instant startAt) throws Exception {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                UUID.fromString(businessId), UUID.fromString(employeeId), UUID.fromString(serviceId), startAt, null);

        MvcResult createResult = mockMvc.perform(post("/api/v1/appointments")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        String appointmentId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(patch("/api/v1/appointments/" + appointmentId + "/confirm")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk());

        // Directly flip status via repository would be simpler, but exercising the real
        // endpoint keeps this test aligned with actual state-machine rules (Fase 6).
        // complete() requires CONFIRMED and no future-time restriction, so this works
        // even though startAt is in the future in this synthetic test scenario.
        mockMvc.perform(patch("/api/v1/appointments/" + appointmentId + "/complete")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk());

        return appointmentId;
    }

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

    private String createBusiness(String ownerToken) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/businesses")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateBusinessRequest("Barbearia Relatorio", null, null, null, null, "UTC"))))
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
}
