package com.company.salonbooking.employee.application.usecase;

import com.company.salonbooking.business.domain.repository.BusinessRepository;
import com.company.salonbooking.employee.application.command.UpdateEmployeeScheduleCommand;
import com.company.salonbooking.employee.domain.exception.EmployeeNotFoundException;
import com.company.salonbooking.employee.domain.model.Employee;
import com.company.salonbooking.employee.domain.model.EmployeeScheduleInterval;
import com.company.salonbooking.employee.domain.repository.EmployeeRepository;
import com.company.salonbooking.employee.domain.repository.EmployeeScheduleRepository;
import com.company.salonbooking.shared.exception.UnauthorizedResourceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Authorized either by the OWNER of the business, or by the employee updating their own schedule (Seção 137). */
@Service
public class UpdateEmployeeScheduleUseCase {

    private final EmployeeRepository employeeRepository;
    private final EmployeeScheduleRepository scheduleRepository;
    private final BusinessRepository businessRepository;

    public UpdateEmployeeScheduleUseCase(EmployeeRepository employeeRepository, EmployeeScheduleRepository scheduleRepository,
                                         BusinessRepository businessRepository) {
        this.employeeRepository = employeeRepository;
        this.scheduleRepository = scheduleRepository;
        this.businessRepository = businessRepository;
    }

    @Transactional
    public List<EmployeeScheduleInterval> execute(UpdateEmployeeScheduleCommand command) {
        Employee employee = employeeRepository.findById(command.employeeId())
                .orElseThrow(() -> new EmployeeNotFoundException(command.employeeId()));

        boolean isSelf = employee.isUser(command.requesterId());
        boolean isOwner = businessRepository.findById(employee.getBusinessId())
                .map(b -> b.isOwnedBy(command.requesterId()))
                .orElse(false);

        if (!isSelf && !isOwner) {
            throw new UnauthorizedResourceException("You cannot modify this employee's schedule.");
        }

        validateNoOverlaps(command.intervals());
        return scheduleRepository.replaceAll(command.employeeId(), command.intervals());
    }

    private void validateNoOverlaps(List<EmployeeScheduleInterval> intervals) {
        Map<DayOfWeek, List<EmployeeScheduleInterval>> byDay = intervals.stream()
                .collect(Collectors.groupingBy(EmployeeScheduleInterval::getDayOfWeek));

        for (List<EmployeeScheduleInterval> dayIntervals : byDay.values()) {
            for (int i = 0; i < dayIntervals.size(); i++) {
                for (int j = i + 1; j < dayIntervals.size(); j++) {
                    if (dayIntervals.get(i).getTimeRange().overlaps(dayIntervals.get(j).getTimeRange())) {
                        throw new IllegalArgumentException(
                                "Schedule intervals overlap on " + dayIntervals.get(i).getDayOfWeek());
                    }
                }
            }
        }
    }
}