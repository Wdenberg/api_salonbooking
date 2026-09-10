package com.company.salonbooking.employee.application.command;

import com.company.salonbooking.employee.domain.model.EmployeeScheduleInterval;

import java.util.List;
import java.util.UUID;

public record UpdateEmployeeScheduleCommand(
        UUID employeeId,
        UUID requesterId,
        List<EmployeeScheduleInterval> intervals
) {
    public UpdateEmployeeScheduleCommand {
        intervals = intervals == null ? List.of() : List.copyOf(intervals);
    }
}
