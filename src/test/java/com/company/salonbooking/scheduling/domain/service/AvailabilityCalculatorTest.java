package com.company.salonbooking.scheduling.domain.service;

import com.company.salonbooking.shared.domain.model.TimeRange;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AvailabilityCalculatorTest {

    @Test
    void deveGerarSlotsRespeitandoIntersecaoDeHorarios() {
        LocalDate date = LocalDate.of(2026, 8, 17); // a Monday
        List<TimeRange> businessHours = List.of(new TimeRange(LocalTime.of(8, 0), LocalTime.of(18, 0)));
        List<TimeRange> employeeSchedule = List.of(new TimeRange(LocalTime.of(9, 0), LocalTime.of(12, 0)));

        List<com.company.salonbooking.scheduling.domain.model.TimeSlot> slots = AvailabilityCalculator.calculate(
                date, ZoneOffset.UTC, businessHours, employeeSchedule, List.of(), List.of(),
                30, 30, Instant.parse("2026-08-10T00:00:00Z"), 60);

        // 9:00 to 12:00 in 30-min slots of 30-min duration => 6 slots (9:00, 9:30, ..., 11:30)
        assertThat(slots).hasSize(6);
        assertThat(slots.get(0).start()).isEqualTo(Instant.parse("2026-08-17T09:00:00Z"));
        assertThat(slots.get(slots.size() - 1).start()).isEqualTo(Instant.parse("2026-08-17T11:30:00Z"));
    }

    @Test
    void deveExcluirSlotsBloqueados() {
        LocalDate date = LocalDate.of(2026, 8, 17);
        List<TimeRange> hours = List.of(new TimeRange(LocalTime.of(9, 0), LocalTime.of(11, 0)));

        AvailabilityCalculator.Blocked block = new AvailabilityCalculator.Blocked(
                Instant.parse("2026-08-17T09:30:00Z"), Instant.parse("2026-08-17T10:30:00Z"));

        List<com.company.salonbooking.scheduling.domain.model.TimeSlot> slots = AvailabilityCalculator.calculate(
                date, ZoneOffset.UTC, hours, hours, List.of(block), List.of(),
                30, 30, Instant.parse("2026-08-10T00:00:00Z"), 60);

        assertThat(slots).extracting(com.company.salonbooking.scheduling.domain.model.TimeSlot::start)
                .doesNotContain(Instant.parse("2026-08-17T09:30:00Z"), Instant.parse("2026-08-17T10:00:00Z"));
    }

    @Test
    void naoDeveGerarSlotsQueDesrespeitemAntecedenciaMinima() {
        LocalDate date = LocalDate.of(2026, 8, 17);
        List<TimeRange> hours = List.of(new TimeRange(LocalTime.of(9, 0), LocalTime.of(11, 0)));

        // "now" is 17-Aug 09:15Z; minimum advance is 60 minutes => nothing before 10:15 should appear
        List<com.company.salonbooking.scheduling.domain.model.TimeSlot> slots = AvailabilityCalculator.calculate(
                date, ZoneOffset.UTC, hours, hours, List.of(), List.of(),
                30, 30, Instant.parse("2026-08-17T09:15:00Z"), 60);

        assertThat(slots).allMatch(s -> !s.start().isBefore(Instant.parse("2026-08-17T10:15:00Z")));
    }
}