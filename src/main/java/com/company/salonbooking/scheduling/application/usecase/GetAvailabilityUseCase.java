package com.company.salonbooking.scheduling.application.usecase;

import com.company.salonbooking.business.domain.exception.BusinessNotFoundException;
import com.company.salonbooking.business.domain.model.Business;
import com.company.salonbooking.business.domain.model.BusinessSettings;
import com.company.salonbooking.business.domain.repository.BusinessOpeningHourRepository;
import com.company.salonbooking.business.domain.repository.BusinessRepository;
import com.company.salonbooking.business.domain.repository.BusinessSettingsRepository;
import com.company.salonbooking.catalog.domain.exception.ServiceOfferingNotFoundException;
import com.company.salonbooking.catalog.domain.model.ServiceOffering;
import com.company.salonbooking.catalog.domain.repository.ServiceOfferingRepository;
import com.company.salonbooking.employee.domain.model.Employee;
import com.company.salonbooking.employee.domain.repository.AvailabilityBlockRepository;
import com.company.salonbooking.employee.domain.repository.EmployeeRepository;
import com.company.salonbooking.employee.domain.repository.EmployeeScheduleRepository;
import com.company.salonbooking.scheduling.application.query.GetAvailabilityQuery;
import com.company.salonbooking.scheduling.domain.model.Appointment;
import com.company.salonbooking.scheduling.domain.model.TimeSlot;
import com.company.salonbooking.scheduling.domain.repository.AppointmentRepository;
import com.company.salonbooking.scheduling.domain.service.AvailabilityCalculator;
import com.company.salonbooking.shared.domain.model.TimeRange;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Never cached (Seção 37): availability depends on data that changes on every booking.
 * The database remains the single source of truth; this only produces a prediction.
 */
@Service
public class GetAvailabilityUseCase {

    private final BusinessRepository businessRepository;
    private final BusinessSettingsRepository businessSettingsRepository;
    private final BusinessOpeningHourRepository openingHourRepository;
    private final ServiceOfferingRepository serviceRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeScheduleRepository employeeScheduleRepository;
    private final AvailabilityBlockRepository availabilityBlockRepository;
    private final AppointmentRepository appointmentRepository;
    private final java.time.Clock clock;

    public GetAvailabilityUseCase(BusinessRepository businessRepository, BusinessSettingsRepository businessSettingsRepository,
                                  BusinessOpeningHourRepository openingHourRepository, ServiceOfferingRepository serviceRepository,
                                  EmployeeRepository employeeRepository, EmployeeScheduleRepository employeeScheduleRepository,
                                  AvailabilityBlockRepository availabilityBlockRepository, AppointmentRepository appointmentRepository,
                                  java.time.Clock clock) {
        this.businessRepository = businessRepository;
        this.businessSettingsRepository = businessSettingsRepository;
        this.openingHourRepository = openingHourRepository;
        this.serviceRepository = serviceRepository;
        this.employeeRepository = employeeRepository;
        this.employeeScheduleRepository = employeeScheduleRepository;
        this.availabilityBlockRepository = availabilityBlockRepository;
        this.appointmentRepository = appointmentRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Map<UUID, List<TimeSlot>> execute(GetAvailabilityQuery query) {
        Business business = businessRepository.findById(query.businessId())
                .orElseThrow(() -> new BusinessNotFoundException(query.businessId()));

        ServiceOffering service = serviceRepository.findById(query.serviceId())
                .orElseThrow(() -> new ServiceOfferingNotFoundException(query.serviceId()));

        BusinessSettings settings = businessSettingsRepository.findByBusinessId(business.getId())
                .orElseThrow(() -> new BusinessNotFoundException(business.getId()));

        List<Employee> employees = query.employeeId() != null
                ? employeeRepository.findById(query.employeeId()).filter(Employee::isActive).map(List::of).orElse(List.of())
                : employeeRepository.findActiveByBusinessId(business.getId());

        List<TimeRange> businessHoursForDay = openingHourRepository.findByBusinessId(business.getId()).stream()
                .filter(h -> h.getDayOfWeek() == query.date().getDayOfWeek())
                .map(h -> h.getTimeRange())
                .toList();

        Instant dayStart = ZonedDateTime.of(query.date().atStartOfDay(), business.getTimezone()).toInstant();
        Instant dayEnd = ZonedDateTime.of(query.date().plusDays(1).atStartOfDay(), business.getTimezone()).toInstant();

        Map<UUID, List<TimeSlot>> result = new LinkedHashMap<>();

        for (Employee employee : employees) {
            List<TimeRange> employeeScheduleForDay = employeeScheduleRepository.findByEmployeeId(employee.getId()).stream()
                    .filter(s -> s.getDayOfWeek() == query.date().getDayOfWeek())
                    .map(s -> s.getTimeRange())
                    .toList();

            List<AvailabilityCalculator.Blocked> blocks = availabilityBlockRepository
                    .findByEmployeeIdAndRange(employee.getId(), dayStart, dayEnd).stream()
                    .map(b -> new AvailabilityCalculator.Blocked(b.getStartAt(), b.getEndAt()))
                    .toList();

            List<AvailabilityCalculator.Blocked> existing = appointmentRepository
                    .findActiveByEmployeeAndRange(employee.getId(), dayStart, dayEnd).stream()
                    .map(a -> new AvailabilityCalculator.Blocked(a.getStartAt(), a.getEndAt()))
                    .toList();

            List<TimeSlot> slots = AvailabilityCalculator.calculate(
                    query.date(), business.getTimezone(), businessHoursForDay, employeeScheduleForDay,
                    blocks, existing, service.getDuration().toMinutes(), settings.getSlotIntervalMinutes(),
                    Instant.now(clock), settings.getMinimumAdvanceMinutes());

            if (!slots.isEmpty()) {
                result.put(employee.getId(), slots);
            }
        }

        return result;
    }
}