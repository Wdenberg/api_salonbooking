package com.company.salonbooking.scheduling.interfaces.rest;

import com.company.salonbooking.AbstractIntegrationTest;
import com.company.salonbooking.business.interfaces.rest.dto.CreateBusinessRequest;
import com.company.salonbooking.business.interfaces.rest.dto.OpeningHourIntervalDto;
import com.company.salonbooking.catalog.interfaces.rest.dto.CreateServiceRequest;
import com.company.salonbooking.employee.interfaces.rest.dto.CreateEmployeeRequest;
import com.company.salonbooking.employee.interfaces.rest.dto.EmployeeScheduleIntervalDto;
import com.company.salonbooking.identity.interfaces.rest.dto.RegisterCustomerRequest;
import com.company.salonbooking.identity.interfaces.rest.dto.RegisterOwnerRequest;
import com.company.salonbooking.scheduling.domain.model.Appointment;
import com.company.salonbooking.scheduling.domain.repository.AppointmentRepository;
import com.company.salonbooking.scheduling.interfaces.rest.dto.CreateAppointmentRequest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AppointmentConcurrencyIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private AppointmentRepository appointmentRepository; // <-- ESSENCIAL: @Autowired

    @Test
    void dezRequisicoesSimultaneasParaMesmoHorario_apenasUmaDeveSerCriada() throws Exception {
        // Setup
        String ownerToken = registerOwnerAndGetToken("owner-concurrency@example.com");
        String businessId = createBusiness(ownerToken);
        setOpeningHours(ownerToken, businessId);

        String employeeId = createEmployee(ownerToken, businessId, "barbeiro-concurrency@example.com");
        setEmployeeSchedule(ownerToken, employeeId);

        String serviceId = createService(ownerToken, businessId);
        String customerToken = registerCustomerAndGetToken("cliente-concurrency@example.com");

        Instant startAt = ZonedDateTime.now(ZoneOffset.UTC)
                .plusDays(3)
                .withHour(10)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
                .toInstant();

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                UUID.fromString(businessId),
                UUID.fromString(employeeId),
                UUID.fromString(serviceId),
                startAt,
                "concurrent booking attempt"
        );

        String requestBody = objectMapper.writeValueAsString(request);

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        // (opcional) contadores apenas para diagnóstico, não para asserção
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger conflictCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    int status = mockMvc.perform(post("/api/v1/appointments")
                                    .header("Authorization", "Bearer " + customerToken)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBody))
                            .andReturn()
                            .getResponse()
                            .getStatus();

                    if (status == 201) successCount.incrementAndGet();
                    else if (status == 409) conflictCount.incrementAndGet();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        boolean finished = doneLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(finished).isTrue();

        // VERIFICAÇÃO FINAL: apenas um agendamento deve ter sido persistido
        List<Appointment> appointments = appointmentRepository.findActiveByEmployeeAndRange(
                UUID.fromString(employeeId),
                startAt.minusSeconds(1),
                startAt.plusSeconds(3600)
        );
        assertThat(appointments).hasSize(1);
    }

    // --- Helpers (mesmos da versão anterior) ---
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
                                new CreateBusinessRequest("Barbearia Concorrência", null, null, null, null, "UTC"))))
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