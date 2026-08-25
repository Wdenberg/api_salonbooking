package com.company.salonbooking.scheduling.domain.model;

import com.company.salonbooking.scheduling.domain.exception.CancellationNotAllowedException;
import com.company.salonbooking.scheduling.domain.exception.InvalidAppointmentTransitionException;
import com.company.salonbooking.shared.domain.model.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppointmentTest {

    private Appointment newAppointment(Instant startAt) {
        return Appointment.schedule(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                startAt, startAt.plusSeconds(1800), "obs", "Corte", Money.of(new BigDecimal("40"), "BRL"), 30, "Zé", Instant.now());
    }

    @Test
    void deveIrDePendingParaConfirmed() {
        Appointment appointment = newAppointment(Instant.now().plus(2, ChronoUnit.DAYS));
        appointment.confirm(Instant.now());
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
    }

    @Test
    void naoDeveConfirmarDuasVezes() {
        Appointment appointment = newAppointment(Instant.now().plus(2, ChronoUnit.DAYS));
        appointment.confirm(Instant.now());
        assertThrows(InvalidAppointmentTransitionException.class, () -> appointment.confirm(Instant.now()));
    }

    @Test
    void naoDeveCompletarSemEstarConfirmed() {
        Appointment appointment = newAppointment(Instant.now().plus(2, ChronoUnit.DAYS));
        assertThrows(InvalidAppointmentTransitionException.class, () -> appointment.complete(Instant.now()));
    }

    @Test
    void naoDeveCancelarCompleted() {
        Instant now = Instant.now();
        Appointment appointment = newAppointment(now.plus(2, ChronoUnit.DAYS));
        appointment.confirm(now);
        appointment.complete(now.plus(2, ChronoUnit.DAYS).plusSeconds(1800));

        assertThrows(InvalidAppointmentTransitionException.class, () -> appointment.cancel(now, 60));
    }

    @Test
    void naoDeveCompletarCancelled() {
        Instant now = Instant.now();
        Appointment appointment = newAppointment(now.plus(2, ChronoUnit.DAYS));
        appointment.cancel(now, 60);

        assertThrows(InvalidAppointmentTransitionException.class, () -> appointment.complete(now));
    }

    @Test
    void deveRecusarCancelamentoForaDoPrazo() {
        Instant now = Instant.now();
        Appointment appointment = newAppointment(now.plusSeconds(1800)); // starts in 30 min

        // cancellation policy requires 120 minutes notice
        assertThrows(CancellationNotAllowedException.class, () -> appointment.cancel(now, 120));
    }

    @Test
    void devePermitirCancelamentoDentroDoPrazo() {
        Instant now = Instant.now();
        Appointment appointment = newAppointment(now.plus(3, ChronoUnit.HOURS));

        appointment.cancel(now, 120);
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
    }
}