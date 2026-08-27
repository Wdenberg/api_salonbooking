-- Consumer-side deduplication (Seção 74: "mensagem duplicada não gera efeito duplicado").
-- Complements outbox at-least-once delivery: a redelivered message with an eventId
-- already recorded here is acknowledged and skipped without reprocessing.
CREATE TABLE processed_events (
                                  event_id     UUID NOT NULL,
                                  consumer     VARCHAR(150) NOT NULL,
                                  processed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                  PRIMARY KEY (event_id, consumer)
);

CREATE INDEX idx_processed_events_consumer ON processed_events (consumer);