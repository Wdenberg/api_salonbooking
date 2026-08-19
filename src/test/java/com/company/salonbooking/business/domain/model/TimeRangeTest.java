package com.company.salonbooking.business.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TimeRangeTest {

    @Test
    void deveRecusarStartMaiorOuIgualEnd() {
        assertThrows(IllegalArgumentException.class, () -> new TimeRange(LocalTime.of(18, 0), LocalTime.of(8, 0)));
    }

    @Test
    void deveDetectarOverlap() {
        TimeRange manha = new TimeRange(LocalTime.of(8, 0), LocalTime.of(12, 0));
        TimeRange almocoConflitante = new TimeRange(LocalTime.of(11, 0), LocalTime.of(13, 0));

        assertThat(manha.overlaps(almocoConflitante)).isTrue();
    }

    @Test
    void naoDeveDetectarOverlapEmIntervalosDisjuntos() {
        TimeRange manha = new TimeRange(LocalTime.of(8, 0), LocalTime.of(12, 0));
        TimeRange tarde = new TimeRange(LocalTime.of(13, 0), LocalTime.of(18, 0));

        assertThat(manha.overlaps(tarde)).isFalse();
    }
}