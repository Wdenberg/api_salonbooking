package com.company.salonbooking.infrastructure.outbox;

/** Reservado para um futuro fluxo de publicação assíncrona, no qual o bloqueio da linha é liberado antes
 * da conclusão da confirmação (ack) do broker (Seção 26). O despacho síncrono atual — publicar
 * e registrar o resultado dentro da mesma transação bloqueada — não necessita desse estado,
 * uma vez que o SKIP LOCKED, por si só, já garante a exclusão mútua entre publicadores. */

public enum OutboxStatus {
    PENDING,
    PROCESSING,
    PUBLISHED,
    FAILED
}