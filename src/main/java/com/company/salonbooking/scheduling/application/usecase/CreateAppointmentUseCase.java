package com.company.salonbooking.scheduling.application.usecase;

import com.company.salonbooking.business.domain.exception.BusinessNotFoundException;
import com.company.salonbooking.business.domain.model.Business;
import com.company.salonbooking.business.domain.model.BusinessSettings;
import com.company.salonbooking.business.domain.model.OpeningHourInterval;
import com.company.salonbooking.business.domain.repository.BusinessOpeningHourRepository;
import com.company.salonbooking.business.domain.repository.BusinessRepository;
import com.company.salonbooking.business.domain.repository.BusinessSettingsRepository;
import com.company.salonbooking.catalog.domain.exception.ServiceOfferingNotFoundException;
import com.company.salonbooking.catalog.domain.model.ServiceOffering;
import com.company.salonbooking.catalog.domain.repository.ServiceOfferingRepository;
import com.company.salonbooking.employee.domain.exception.EmployeeNotFoundException;
import com.company.salonbooking.employee.domain.model.Employee;
import com.company.salonbooking.employee.domain.model.EmployeeScheduleInterval;
import com.company.salonbooking.employee.domain.repository.AvailabilityBlockRepository;
import com.company.salonbooking.employee.domain.repository.EmployeeRepository;
import com.company.salonbooking.employee.domain.repository.EmployeeScheduleRepository;
import com.company.salonbooking.scheduling.application.command.CreateAppointmentCommand;
import com.company.salonbooking.scheduling.application.port.EmployeeNameResolver;
import com.company.salonbooking.scheduling.domain.event.AppointmentCreatedEvent;
import com.company.salonbooking.scheduling.domain.exception.AppointmentConflictException;
import com.company.salonbooking.scheduling.domain.exception.SchedulingRuleViolationException;
import com.company.salonbooking.scheduling.domain.model.Appointment;
import com.company.salonbooking.scheduling.domain.repository.AppointmentRepository;
import com.company.salonbooking.shared.application.port.DomainEventPublisher;
import com.company.salonbooking.shared.domain.model.TimeRange;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;


/**
 * Implementa o limite da transação CreateAppointment descrito na Seção 25:
 * validar -> criar -> persistir agendamento -> (Outbox entra na Fase 8) -> efetivar (commit).
 * A restrição de exclusão é a autoridade final contra agendamentos duplicados; as verificações
 * deste caso de uso existem para permitir uma falha rápida com uma mensagem clara, antes
 * mesmo de acessar o banco de dados, para os casos comuns (horário passado, fora do expediente,
 * recursos inativos).
 */

@Service
public class CreateAppointmentUseCase {

    private final BusinessRepository businessRepository;
    private final BusinessSettingsRepository businessSettingsRepository;
    private final BusinessOpeningHourRepository openingHourRepository;
    private final ServiceOfferingRepository serviceRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeScheduleRepository employeeScheduleRepository;
    private final AvailabilityBlockRepository availabilityBlockRepository;
    private final AppointmentRepository appointmentRepository;
    private final EmployeeNameResolver employeeNameResolver;
    private final DomainEventPublisher domainEventPublisher;
    private final Clock clock;

    public CreateAppointmentUseCase(BusinessRepository businessRepository, BusinessSettingsRepository businessSettingsRepository,
                                    BusinessOpeningHourRepository openingHourRepository, ServiceOfferingRepository serviceRepository,
                                    EmployeeRepository employeeRepository, EmployeeScheduleRepository employeeScheduleRepository,
                                    AvailabilityBlockRepository availabilityBlockRepository, AppointmentRepository appointmentRepository,
                                    EmployeeNameResolver employeeNameResolver, DomainEventPublisher domainEventPublisher, Clock clock) {
        this.businessRepository = businessRepository;
        this.businessSettingsRepository = businessSettingsRepository;
        this.openingHourRepository = openingHourRepository;
        this.serviceRepository = serviceRepository;
        this.employeeRepository = employeeRepository;
        this.employeeScheduleRepository = employeeScheduleRepository;
        this.availabilityBlockRepository = availabilityBlockRepository;
        this.appointmentRepository = appointmentRepository;
        this.employeeNameResolver = employeeNameResolver;
        this.domainEventPublisher = domainEventPublisher;
        this.clock = clock;
    }

    @Transactional
    public Appointment execute(CreateAppointmentCommand command) {
        Business business = businessRepository.findById(command.businessId())
                .orElseThrow(() -> new BusinessNotFoundException(command.businessId()));
        if (!business.isActive()) {
            throw new SchedulingRuleViolationException("Business is not active.");
        }

        Employee employee = employeeRepository.findById(command.employeeId())
                .orElseThrow(() -> new EmployeeNotFoundException(command.employeeId()));
        if (!employee.belongsToBusiness(business.getId())) {
            throw new SchedulingRuleViolationException("Employee does not belong to this business.");
        }
        if (!employee.isActive()) {
            throw new SchedulingRuleViolationException("Employee is not active.");
        }

        ServiceOffering service = serviceRepository.findById(command.serviceId())
                .orElseThrow(() -> new ServiceOfferingNotFoundException(command.serviceId()));
        if (!service.belongsToBusiness(business.getId())) {
            throw new SchedulingRuleViolationException("Service does not belong to this business.");
        }
        if (!service.isActive()) {
            throw new SchedulingRuleViolationException("Service is not active.");
        }

        BusinessSettings settings = businessSettingsRepository.findByBusinessId(business.getId())
                .orElseThrow(() -> new BusinessNotFoundException(business.getId()));

        Instant now = Instant.now(clock);
        Instant startAt = command.startAt();
        Instant endAt = startAt.plusSeconds(service.getDuration().asJavaDuration().toSeconds());

        validateAdvanceNotice(startAt, now, settings);
        validateWithinBusinessHours(business.getTimezone(), startAt, endAt, business.getId());
        validateWithinEmployeeSchedule(business.getTimezone(), startAt, endAt, employee.getId());
        validateNoBlockConflict(employee.getId(), startAt, endAt);

        if (appointmentRepository.existsOverlapping(employee.getId(), startAt, endAt)) {
            throw new AppointmentConflictException();
        }

        String employeeName = employeeNameResolver.resolveName(employee.getId());

        Appointment appointment = Appointment.schedule(UUID.randomUUID(), business.getId(), command.requesterId(),
                employee.getId(), service.getId(), startAt, endAt, command.notes(),
                service.getName(), service.getPrice(), service.getDuration().toMinutes(), employeeName, now);

        Appointment saved = appointmentRepository.save(appointment);

        // Same transaction as the insert above (Seção 25/26): if the commit fails for any
        // reason, the outbox row never persists either — appointment and event are atomic.
        domainEventPublisher.publish(new AppointmentCreatedEvent(
                saved.getId(), saved.getBusinessId(), saved.getCustomerId(), saved.getEmployeeId(),
                saved.getServiceId(), saved.getStartAt(), saved.getEndAt()));

        return saved;
    }

    private void validateAdvanceNotice(Instant startAt, Instant now, BusinessSettings settings) {
        if (!startAt.isAfter(now)) {
            throw new SchedulingRuleViolationException("Appointment start time must be in the future.");
        }

        Duration untilStart = Duration.between(now, startAt);
        if (untilStart.toMinutes() < settings.getMinimumAdvanceMinutes()) {
            throw new SchedulingRuleViolationException(
                    "Appointment must be booked at least " + settings.getMinimumAdvanceMinutes() + " minutes in advance.");
        }

        if (untilStart.toDays() > settings.getMaximumAdvanceDays()) {
            throw new SchedulingRuleViolationException(
                    "Appointment cannot be booked more than " + settings.getMaximumAdvanceDays() + " days in advance.");
        }
    }

    private void validateWithinBusinessHours(ZoneId zone, Instant startAt, Instant endAt, UUID businessId) {
        List<OpeningHourInterval> allHours = openingHourRepository.findByBusinessId(businessId);
        if (!withinAnyRange(zone, startAt, endAt, allHours.stream().filter(h -> matchesDay(zone, startAt, h.getDayOfWeek()))
                .map(OpeningHourInterval::getTimeRange).toList())) {
            throw new SchedulingRuleViolationException("Appointment falls outside business opening hours.");
        }
    }

    private void validateWithinEmployeeSchedule(ZoneId zone, Instant startAt, Instant endAt, UUID employeeId) {
        List<EmployeeScheduleInterval> allSchedule = employeeScheduleRepository.findByEmployeeId(employeeId);
        if (!withinAnyRange(zone, startAt, endAt, allSchedule.stream()
                .filter(s -> matchesDay(zone, startAt, s.getDayOfWeek()))
                .map(EmployeeScheduleInterval::getTimeRange).toList())) {
            throw new SchedulingRuleViolationException("Appointment falls outside employee's working schedule.");
        }
    }

    private void validateNoBlockConflict(UUID employeeId, Instant startAt, Instant endAt) {
        boolean blocked = availabilityBlockRepository.findByEmployeeIdAndRange(employeeId, startAt, endAt).stream()
                .anyMatch(b -> b.overlaps(startAt, endAt));
        if (blocked) {
            throw new SchedulingRuleViolationException("Employee is unavailable during the selected time.");
        }
    }

    private boolean matchesDay(ZoneId zone, Instant instant, java.time.DayOfWeek dayOfWeek) {
        return ZonedDateTime.ofInstant(instant, zone).getDayOfWeek() == dayOfWeek;
    }

    private boolean withinAnyRange(ZoneId zone, Instant startAt, Instant endAt, List<TimeRange> ranges) {
        LocalDate date = ZonedDateTime.ofInstant(startAt, zone).toLocalDate();
        LocalTime startTime = ZonedDateTime.ofInstant(startAt, zone).toLocalTime();
        LocalTime endTime = ZonedDateTime.ofInstant(endAt, zone).toLocalTime();

        if (!ZonedDateTime.ofInstant(endAt, zone).toLocalDate().equals(date)) {
            return false;
        }

        return ranges.stream().anyMatch(r -> !startTime.isBefore(r.start()) && !endTime.isAfter(r.end()));
    }
}