package com.company.salonbooking.scheduling.interfaces.rest;

import com.company.salonbooking.scheduling.application.query.GetAvailabilityQuery;
import com.company.salonbooking.scheduling.application.usecase.GetAvailabilityUseCase;
import com.company.salonbooking.scheduling.interfaces.rest.dto.EmployeeAvailabilityDto;
import com.company.salonbooking.scheduling.interfaces.rest.dto.TimeSlotDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Availability")
public class AvailabilityController {

    private final GetAvailabilityUseCase getAvailabilityUseCase;

    public AvailabilityController(GetAvailabilityUseCase getAvailabilityUseCase) {
        this.getAvailabilityUseCase = getAvailabilityUseCase;
    }

    @GetMapping("/api/v1/businesses/{businessId}/availability")
    public ResponseEntity<List<EmployeeAvailabilityDto>> get(
            @PathVariable UUID businessId,
            @RequestParam UUID serviceId,
            @RequestParam(required = false) UUID employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        var result = getAvailabilityUseCase.execute(new GetAvailabilityQuery(businessId, serviceId, employeeId, date));

        List<EmployeeAvailabilityDto> response = result.entrySet().stream()
                .map(entry -> new EmployeeAvailabilityDto(entry.getKey(),
                        entry.getValue().stream().map(s -> new TimeSlotDto(s.start(), s.end())).toList()))
                .toList();

        return ResponseEntity.ok(response);
    }
}