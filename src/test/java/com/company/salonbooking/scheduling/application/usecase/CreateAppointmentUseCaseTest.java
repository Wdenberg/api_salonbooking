package com.company.salonbooking.scheduling.application.usecase;

import com.company.salonbooking.business.domain.model.Address;
import com.company.salonbooking.business.domain.model.Business;
import com.company.salonbooking.business.domain.model.BusinessSettings;
import com.company.salonbooking.business.domain.repository.BusinessOpeningHourRepository;
import com.company.salonbooking.business.domain.repository.BusinessRepository;
import com.company.salonbooking.business.domain.repository.BusinessSettingsRepository;
import com.company.salonbooking.catalog.domain.model.ServiceOffering;
import com.company.salonbooking.catalog.domain.model.ServiceDuration;
import com.company.salonbooking.catalog.domain.repository.ServiceOfferingRepository;
import com.company.salonbooking.employee.domain.model.Employee;
import com.company.salonbooking.employee.domain.model.EmployeeStatus;
import com.company.salonbooking.employee.domain.repository.AvailabilityBlockRepository;
import com.company.salonbooking.employee.domain.repository.EmployeeRepository;
import com.company.salonbooking.employee.domain.repository.EmployeeScheduleRepository;
import com.company.salonbooking.scheduling.application.command.CreateAppointmentCommand;
import com.company.salonbooking.scheduling.application.port.EmployeeNameResolver;
import com.company.salonbooking.scheduling.domain.exception.SchedulingRuleViolationException;
import com.company.salonbooking.scheduling.domain.repository.AppointmentRepository;
import com.company.salonbooking.shared.application.port.AuditRecorder;
import com.company.salonbooking.shared.application.port.ApplicationMetrics;
import com.company.salonbooking.shared.application.port.DomainEventPublisher;
import com.company.salonbooking.shared.domain.model.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.when;

/**
 * Closes the Fase 15 gap: isolated unit tests for CreateAppointmentUseCase's business
 * rule validations (Seção 70), which were previously only exercised indirectly through
 * slower HTTP integration tests.
 */
@ExtendWith(MockitoExtension.class)
class CreateAppointmentUseCaseTest {

    @Mock private BusinessRepository businessRepository;
    @Mock private BusinessSettingsRepository businessSettingsRepository;
    @Mock private BusinessOpeningHourRepository openingHourRepository;
    @Mock private ServiceOfferingRepository serviceRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private EmployeeScheduleRepository employeeScheduleRepository;
    @Mock private AvailabilityBlockRepository availabilityBlockRepository;
    @Mock private AppointmentRepository appointmentRepository;
    @Mock private EmployeeNameResolver employeeNameResolver;
    @Mock private DomainEventPublisher domainEventPublisher;
    @Mock private AuditRecorder auditRecorder;
    @Mock private ApplicationMetrics appMetrics;
    private CreateAppointmentUseCase useCase;
    private final Clock clock = Clock.fixed(Instant.parse("2026-08-17T09:00:00Z"), ZoneOffset.UTC); // a Monday

    private Business business;
    private Employee employee;
    private ServiceOffering service;
    private BusinessSettings settings;
    private UUID businessId, employeeId, serviceId, customerId;




    @BeforeEach
    void setUp() {
        useCase = new CreateAppointmentUseCase(businessRepository, businessSettingsRepository, openingHourRepository,
                serviceRepository, employeeRepository, employeeScheduleRepository, availabilityBlockRepository,
                appointmentRepository, employeeNameResolver, domainEventPublisher, auditRecorder, appMetrics ,clock);

        businessId = UUID.randomUUID();
        employeeId = UUID.randomUUID();
        serviceId = UUID.randomUUID();
        customerId = UUID.randomUUID();

        business = Business.create(businessId, UUID.randomUUID(), "Barbearia", null, null, null,
                Address.empty(), ZoneOffset.UTC, Instant.now(clock));
        employee = Employee.create(employeeId, UUID.randomUUID(), businessId, "Corte", Instant.now(clock));
        service = ServiceOffering.create(serviceId, businessId, "Corte", null,
                Money.of(new BigDecimal("40"), "BRL"), ServiceDuration.ofMinutes(30), Instant.now(clock));
        settings = BusinessSettings.defaultsFor(businessId, Instant.now(clock));

    }

    private CreateAppointmentCommand commandAt(Instant startAt) {
        return new CreateAppointmentCommand(customerId, businessId, employeeId, serviceId, startAt, null);
    }

    @Test
    void deveRecusarHorarioNoPassado() {
        givenBaseDependencies();
        Instant pastTime = Instant.now(clock).minusSeconds(3600);
        assertThrows(SchedulingRuleViolationException.class, () -> useCase.execute(commandAt(pastTime)));
    }

    @Test
    void deveRecusarAntecedenciaMenorQueOMinimo() {
        givenBaseDependencies();
        // settings default: minimumAdvanceMinutes = 60
        Instant tooSoon = Instant.now(clock).plusSeconds(30 * 60); // only 30 minutes ahead
        assertThrows(SchedulingRuleViolationException.class, () -> useCase.execute(commandAt(tooSoon)));
    }

    @Test
    void deveRecusarAntecedenciaMaiorQueOMaximo() {
        givenBaseDependencies();
        // settings default: maximumAdvanceDays = 30
        Instant tooFar = Instant.now(clock).plus(60, ChronoUnit.DAYS);
        assertThrows(SchedulingRuleViolationException.class, () -> useCase.execute(commandAt(tooFar)));
    }

    @Test
    void deveRecusarFuncionarioInativo() {
        when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));
        Employee inactive = Employee.create(employeeId, UUID.randomUUID(), businessId, "Corte", Instant.now(clock));
        inactive.changeStatus(EmployeeStatus.INACTIVE, Instant.now(clock));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(inactive));

        Instant validTime = Instant.now(clock).plus(3, ChronoUnit.DAYS);
        assertThrows(SchedulingRuleViolationException.class, () -> useCase.execute(commandAt(validTime)));
    }

    @Test
    void deveRecusarServicoInativo() {
        when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
        service.deactivate(Instant.now(clock));

        Instant validTime = Instant.now(clock).plus(3, ChronoUnit.DAYS);
        assertThrows(SchedulingRuleViolationException.class, () -> useCase.execute(commandAt(validTime)));
    }

    @Test
    void deveRecusarBusinessInativo() {
        when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));
        business.changeStatus(com.company.salonbooking.business.domain.model.BusinessStatus.SUSPENDED, Instant.now(clock));

        Instant validTime = Instant.now(clock).plus(3, ChronoUnit.DAYS);
        assertThrows(SchedulingRuleViolationException.class, () -> useCase.execute(commandAt(validTime)));
    }

    @Test
    void deveRecusarQuandoForaDoExpedienteDoNegocio() {
        givenBaseDependencies();
        when(openingHourRepository.findByBusinessId(businessId)).thenReturn(List.of()); // no opening hours at all

        Instant validAdvance = Instant.now(clock).plus(3, ChronoUnit.DAYS);
        assertThrows(SchedulingRuleViolationException.class, () -> useCase.execute(commandAt(validAdvance)));
    }

    private void givenBaseDependencies() {
        when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
        when(businessSettingsRepository.findByBusinessId(businessId)).thenReturn(Optional.of(settings));
    }
}