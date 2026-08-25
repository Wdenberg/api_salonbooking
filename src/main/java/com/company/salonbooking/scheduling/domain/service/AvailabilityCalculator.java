package com.company.salonbooking.scheduling.domain.service;

import com.company.salonbooking.scheduling.domain.model.TimeSlot;
import com.company.salonbooking.shared.domain.model.TimeRange;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Pure domain algorithm (Seção 86). No Spring, no persistence — takes already-loaded
 * inputs and produces candidate slots. This makes it trivially unit-testable and keeps
 * the "what is a valid slot" rule in one place instead of scattered across use cases.
 *
 * Availability here is only a PREDICTION (Seção 86, last line): the final booking is
 * still re-validated inside the transaction, and ultimately guaranteed by the database
 * exclusion constraint.
 */
public final class AvailabilityCalculator {

    private AvailabilityCalculator() {
    }

    public record Blocked(Instant start, Instant end) {}

    public static List<TimeSlot> calculate(
            LocalDate date,
            ZoneId businessZone,
            List<TimeRange> businessHoursForDay,
            List<TimeRange> employeeScheduleForDay,
            List<Blocked> employeeBlocks,
            List<Blocked> existingAppointments,
            int serviceDurationMinutes,
            int slotIntervalMinutes,
            Instant now,
            long minimumAdvanceMinutes) {

        List<TimeRange> availableRanges = intersect(businessHoursForDay, employeeScheduleForDay);
        Instant earliestAllowed = now.plusSeconds(minimumAdvanceMinutes * 60);

        List<TimeSlot> result = new ArrayList<>();

        for (TimeRange range : availableRanges) {
            LocalTime cursor = range.start();

            while (true) {
                LocalTime slotEndTime = cursor.plusMinutes(serviceDurationMinutes);
                if (slotEndTime.isAfter(range.end())) {
                    break;
                }

                Instant slotStart = ZonedDateTime.of(date, cursor, businessZone).toInstant();
                Instant slotEnd = ZonedDateTime.of(date, slotEndTime, businessZone).toInstant();

                boolean respectsAdvanceNotice = !slotStart.isBefore(earliestAllowed);
                boolean blocked = employeeBlocks.stream().anyMatch(b -> overlaps(b.start(), b.end(), slotStart, slotEnd));
                boolean taken = existingAppointments.stream().anyMatch(a -> overlaps(a.start(), a.end(), slotStart, slotEnd));

                if (respectsAdvanceNotice && !blocked && !taken) {
                    result.add(new TimeSlot(slotStart, slotEnd));
                }

                cursor = cursor.plusMinutes(slotIntervalMinutes);
                // LocalTime wraps at midnight; plusMinutes never goes "backwards" unless the
                // range itself would cross midnight, which opening-hour intervals do not (Seção 139).
                if (!cursor.isAfter(range.start()) && cursor.equals(LocalTime.MIDNIGHT)) {
                    break;
                }
            }
        }

        result.sort((a, b) -> a.start().compareTo(b.start()));
        return result;
    }

    /** Intersects two lists of same-day LocalTime ranges (e.g. business hours ∩ employee schedule). */
    private static List<TimeRange> intersect(List<TimeRange> a, List<TimeRange> b) {
        List<TimeRange> result = new ArrayList<>();
        for (TimeRange rangeA : a) {
            for (TimeRange rangeB : b) {
                LocalTime start = rangeA.start().isAfter(rangeB.start()) ? rangeA.start() : rangeB.start();
                LocalTime end = rangeA.end().isBefore(rangeB.end()) ? rangeA.end() : rangeB.end();
                if (start.isBefore(end)) {
                    result.add(new TimeRange(start, end));
                }
            }
        }
        return result;
    }

    private static boolean overlaps(Instant startA, Instant endA, Instant startB, Instant endB) {
        return startA.isBefore(endB) && startB.isBefore(endA);
    }
}