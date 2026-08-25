package com.company.salonbooking.infrastructure.outbox;

/**
 * Função pura, extraída para testes unitários (tentativa com recuo exponencial da Seção 32: 5s, 30s, 5min, ...).
 * tentativa=1 -> initialBackoffSeconds; cada tentativa subsequente aproximadamente dobra o tempo, limitado a maxBackoffSeconds.
 */
final class OutboxBackoffCalculator {

    private OutboxBackoffCalculator() {
    }

    static long computeBackoffSeconds(int attempt, long initialBackoffSeconds, long maxBackoffSeconds) {
        int exponent = Math.max(0, attempt - 1);
        long backoff = initialBackoffSeconds * (1L << Math.min(exponent, 20)); // guard against overflow
        return Math.min(backoff, maxBackoffSeconds);
    }
}