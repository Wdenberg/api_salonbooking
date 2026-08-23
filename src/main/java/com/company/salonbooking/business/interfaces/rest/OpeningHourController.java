package com.company.salonbooking.business.interfaces.rest;

import com.company.salonbooking.business.application.command.UpdateOpeningHoursCommand;
import com.company.salonbooking.business.application.usecase.GetOpeningHoursUseCase;
import com.company.salonbooking.business.application.usecase.UpdateOpeningHoursUseCase;
import com.company.salonbooking.business.domain.model.OpeningHourInterval;
import com.company.salonbooking.shared.domain.model.TimeRange;
import com.company.salonbooking.business.interfaces.rest.dto.OpeningHourIntervalDto;
import com.company.salonbooking.infrastructure.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/businesses/{businessId}/opening-hours")
@Tag(name = "Business Opening Hours")
public class OpeningHourController {

    private final GetOpeningHoursUseCase getUseCase;
    private final UpdateOpeningHoursUseCase updateUseCase;

    public OpeningHourController(GetOpeningHoursUseCase getUseCase, UpdateOpeningHoursUseCase updateUseCase) {
        this.getUseCase = getUseCase;
        this.updateUseCase = updateUseCase;
    }

    @GetMapping
    public ResponseEntity<List<OpeningHourIntervalDto>> get(@PathVariable UUID businessId) {
        List<OpeningHourIntervalDto> response = getUseCase.execute(businessId).stream()
                .map(i -> new OpeningHourIntervalDto(i.getDayOfWeek(), i.getTimeRange().start(), i.getTimeRange().end()))
                .toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<OpeningHourIntervalDto>> update(@PathVariable UUID businessId,
                                                               @Valid @RequestBody List<OpeningHourIntervalDto> request,
                                                               @AuthenticationPrincipal AuthenticatedUser principal) {
        List<OpeningHourInterval> intervals = request.stream()
                .map(dto -> new OpeningHourInterval(UUID.randomUUID(), dto.dayOfWeek(),
                        new TimeRange(dto.openTime(), dto.closeTime())))
                .toList();

        List<OpeningHourIntervalDto> response = updateUseCase.execute(
                        new UpdateOpeningHoursCommand(businessId, principal.userId(), intervals)).stream()
                .map(i -> new OpeningHourIntervalDto(i.getDayOfWeek(), i.getTimeRange().start(), i.getTimeRange().end()))
                .toList();

        return ResponseEntity.ok(response);
    }
}