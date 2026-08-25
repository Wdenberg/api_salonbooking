package com.company.salonbooking.catalog.domain.model;

import com.company.salonbooking.shared.domain.model.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyTest {

    @Test
    void deveNormalizarEscalaConformeMoeda() {
        Money money = Money.of(new BigDecimal("45"), "BRL");

        assertThat(money.getAmount()).isEqualByComparingTo("45.00");
        assertThat(money.getCurrencyCode()).isEqualTo("BRL");
    }

    @Test
    void deveRecusarValorNegativo() {
        assertThrows(IllegalArgumentException.class, () -> Money.of(new BigDecimal("-1"), "BRL"));
    }

    @Test
    void doisValoresIguaisDevemSerIguais() {
        Money a = Money.of(new BigDecimal("50.00"), "BRL");
        Money b = Money.of(new BigDecimal("50"), "BRL");

        assertThat(a).isEqualTo(b);
    }
}