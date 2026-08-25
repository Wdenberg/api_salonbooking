package com.company.salonbooking.infrastructure.idempotency;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequestFingerprintTest {

    @Test
    void deveGerarMesmoHashParaMesmoConteudo() {
        String hashA = RequestFingerprint.of("{\"a\":1}");
        String hashB = RequestFingerprint.of("{\"a\":1}");

        assertThat(hashA).isEqualTo(hashB);
    }

    @Test
    void deveGerarHashesDiferentesParaConteudosDiferentes() {
        String hashA = RequestFingerprint.of("{\"a\":1}");
        String hashB = RequestFingerprint.of("{\"a\":2}");

        assertThat(hashA).isNotEqualTo(hashB);
    }
}