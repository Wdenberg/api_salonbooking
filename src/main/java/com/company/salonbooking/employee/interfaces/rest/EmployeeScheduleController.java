package com.company.salonbooking.employee.interfaces.rest;

import com.company.salonbooking.employee.application.command.UpdateEmployeeScheduleCommand;
import com.company.salonbooking.employee.application.usecase.GetEmployeeScheduleUseCase;
import com.company.salonbooking.employee.application.usecase.UpdateEmployeeScheduleUseCase;
import com.company.salonbooking.employee.domain.model.EmployeeScheduleInterval;
import com.company.salonbooking.employee.interfaces.rest.dto.EmployeeScheduleIntervalDto;
import com.company.salonbooking.infrastructure.security.AuthenticatedUser;
import com.company.salonbooking.shared.domain.model.TimeRange;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees/{employeeId}/schedule")
@Tag(name = "Employee Schedule")
public class EmployeeScheduleController {

    private final GetEmployeeScheduleUseCase getUseCase;
    private final UpdateEmployeeScheduleUseCase updateUseCase;

    public EmployeeScheduleController(GetEmployeeScheduleUseCase getUseCase, UpdateEmployeeScheduleUseCase updateUseCase) {
        this.getUseCase = getUseCase;
        this.updateUseCase = updateUseCase;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeScheduleIntervalDto>> get(@PathVariable UUID employeeId) {
        List<EmployeeScheduleIntervalDto> response = getUseCase.execute(employeeId).stream()
                .map(i -> new EmployeeScheduleIntervalDto(i.getDayOfWeek(), i.getTimeRange().start(), i.getTimeRange().end()))
                .toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('OWNER','EMPLOYEE')")
    public ResponseEntity<List<EmployeeScheduleIntervalDto>> update(@PathVariable UUID employeeId,
                                                                    @Valid @RequestBody List<EmployeeScheduleIntervalDto> request,
                                                                    @AuthenticationPrincipal AuthenticatedUser principal) {
        List<EmployeeScheduleInterval> intervals = request.stream()
                .map(dto -> new EmployeeScheduleInterval(UUID.randomUUID(), dto.dayOfWeek(),
                        new TimeRange(dto.startTime(), dto.endTime())))
                .toList();

        List<EmployeeScheduleIntervalDto> response = updateUseCase.execute(
                        new UpdateEmployeeScheduleCommand(employeeId, principal.userId(), intervals)).stream()
                .map(i -> new EmployeeScheduleIntervalDto(i.getDayOfWeek(), i.getTimeRange().start(), i.getTimeRange().end()))
                .toList();

        return ResponseEntity.ok(response);
    }
}