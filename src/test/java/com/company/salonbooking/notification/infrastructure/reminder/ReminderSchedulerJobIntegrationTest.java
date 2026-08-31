package com.company.salonbooking.notification.infrastructure.reminder;

import com.company.salonbooking.AbstractIntegrationTest;
import com.company.salonbooking.business.interfaces.rest.dto.CreateBusinessRequest;
import com.company.salonbooking.business.interfaces.rest.dto.OpeningHourIntervalDto;
import com.company.salonbooking.catalog.interfaces.rest.dto.CreateServiceRequest;
import com.company.salonbooking.employee.interfaces.rest.dto.CreateEmployeeRequest;
import com.company.salonbooking.employee.interfaces.rest.dto.EmployeeScheduleIntervalDto;
import com.company.salonbooking.identity.interfaces.rest.dto.RegisterCustomerRequest;
import com.company.salonbooking.identity.interfaces.rest.dto.RegisterOwnerRequest;
import com.company.salonbooking.infrastructure.outbox.OutboxEventJpaRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReminderSchedulerJobIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ReminderSchedulerJob reminderSchedulerJob;
    @Autowired private ReminderDispatchLogJpaRepository dispatchLogRepository;
    @Autowired private OutboxEventJpaRepository outboxRepository;

    @Test
    void deveGerarLembreteH2ParaAgendamentoConfirmadoDentroDaJanela() throws Exception {
        String ownerToken = registerOwnerAndGetToken("owner-rem1@example.com");
        String businessId = createBusiness(ownerToken);
        setOpeningHours(ownerToken, businessId);
        String employeeId = createEmployee(ownerToken, businessId, "barbeiro-rem1@example.com");
        setEmployeeSchedule(ownerToken, employeeId);
        String serviceId = createService(ownerToken, businessId);
        String customerToken = registerCustomerAndGetToken("cliente-rem1@example.com");

        // Starts in 90 minutes: inside the H2 (120min) window, outside H24.
        Instant startAt = Instant.now().plus(90, ChronoUnit.MINUTES);
        String appointmentId = createAndConfirmAppointment(ownerToken, customerToken, businessId, employeeId, serviceId, startAt);

        reminderSchedulerJob.dispatchDueReminders();

        boolean h2Dispatched = dispatchLogRepository.existsById(
                new ReminderDispatchLogId(UUID.fromString(appointmentId), ReminderType.H2));
        boolean h24Dispatched = dispatchLogRepository.existsById(
                new ReminderDispatchLogId(UUID.fromString(appointmentId), ReminderType.H24));

        assertThat(h2Dispatched).isTrue();
        assertThat(h24Dispatched).isTrue(); // 90min is also < 1440min, so H24 window matches too

        var events = outboxRepository.findByAggregateTypeAndAggregateId("Appointment", UUID.fromString(appointmentId));
        List<String> types = events.stream().map(e -> e.getEventType()).toList();
        assertThat(types).contains("AppointmentReminderRequested");
    }

    @Test
    void naoDeveGerarLembreteDuplicadoEmExecucoesRepetidas() throws Exception {
        String ownerToken = registerOwnerAndGetToken("owner-rem2@example.com");
        String businessId = createBusiness(ownerToken);
        setOpeningHours(ownerToken, businessId);
        String employeeId = createEmployee(ownerToken, businessId, "barbeiro-rem2@example.com");
        setEmployeeSchedule(ownerToken, employeeId);
        String serviceId = createService(ownerToken, businessId);
        String customerToken = registerCustomerAndGetToken("cliente-rem2@example.com");

        Instant startAt = Instant.now().plus(90, ChronoUnit.MINUTES);
        String appointmentId = createAndConfirmAppointment(ownerToken, customerToken, businessId, employeeId, serviceId, startAt);

        reminderSchedulerJob.dispatchDueReminders();
        long countAfterFirstRun = outboxRepository
                .findByAggregateTypeAndAggregateId("Appointment", UUID.fromString(appointmentId)).stream()
                .filter(e -> e.getEventType().equals("AppointmentReminderRequested")).count();

        reminderSchedulerJob.dispatchDueReminders(); // second poll cycle, same appointment still in window
        long countAfterSecondRun = outboxRepository
                .findByAggregateTypeAndAggregateId("Appointment", UUID.fromString(appointmentId)).stream()
                .filter(e -> e.getEventType().equals("AppointmentReminderRequested")).count();

        assertThat(countAfterFirstRun).isEqualTo(2); // H24 + H2, both eligible at 90min out
        assertThat(countAfterSecondRun).isEqualTo(countAfterFirstRun); // no new events on re-poll
    }

    // --- Helpers ---

    private String createAndConfirmAppointment(String ownerToken, String customerToken, String businessId,
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
                                new CreateBusinessRequest("Barbearia Lembrete", null, null, null, null, "UTC"))))
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