package com.company.salonbooking.infrastructure.messaging;

import com.company.salonbooking.business.interfaces.rest.dto.CreateBusinessRequest;
import com.company.salonbooking.business.interfaces.rest.dto.OpeningHourIntervalDto;
import com.company.salonbooking.catalog.interfaces.rest.dto.CreateServiceRequest;
import com.company.salonbooking.employee.interfaces.rest.dto.CreateEmployeeRequest;
import com.company.salonbooking.employee.interfaces.rest.dto.EmployeeScheduleIntervalDto;
import com.company.salonbooking.identity.interfaces.rest.dto.RegisterCustomerRequest;
import com.company.salonbooking.identity.interfaces.rest.dto.RegisterOwnerRequest;
import com.company.salonbooking.infrastructure.outbox.OutboxEventJpaRepository;
import com.company.salonbooking.infrastructure.outbox.OutboxPublisherJob;
import com.company.salonbooking.infrastructure.outbox.OutboxStatus;
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
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AppointmentEventPublishAndConsumeIntegrationTest extends AbstractRabbitMqIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private OutboxEventJpaRepository outboxRepository;
    @Autowired private OutboxPublisherJob outboxPublisherJob;
    @Autowired private ProcessedEventJpaRepository processedEventRepository;

    @Test
    void eventoPublicadoDeveSerConsumidoEMarcadoComoProcessado() throws Exception {
        String ownerToken = registerOwnerAndGetToken("owner-rmq1@example.com");
        String businessId = createBusiness(ownerToken);
        setOpeningHours(ownerToken, businessId);
        String employeeId = createEmployee(ownerToken, businessId, "barbeiro-rmq1@example.com");
        setEmployeeSchedule(ownerToken, employeeId);
        String serviceId = createService(ownerToken, businessId);
        String customerToken = registerCustomerAndGetToken("cliente-rmq1@example.com");

        Instant startAt = Instant.now().plus(3, ChronoUnit.DAYS).truncatedTo(ChronoUnit.HOURS).plus(10, ChronoUnit.HOURS);
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                UUID.fromString(businessId), UUID.fromString(employeeId), UUID.fromString(serviceId), startAt, null);

        MvcResult result = mockMvc.perform(post("/api/v1/appointments")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String appointmentId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

        outboxPublisherJob.dispatchBatch();

        // Confirms the outbox row transitioned to PUBLISHED (delivered to RabbitMQ successfully).
        var events = outboxRepository.findByAggregateTypeAndAggregateId("Appointment", UUID.fromString(appointmentId));
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getStatus()).isEqualTo(OutboxStatus.PUBLISHED);

        // Consumer runs asynchronously; poll until the dedup record appears (Seção 74).
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(processedEventRepository.count()).isGreaterThanOrEqualTo(1));
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

    private String createBusiness(String ownerToken) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/businesses")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateBusinessRequest("Barbearia RMQ", null, null, null, null, "UTC"))))
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