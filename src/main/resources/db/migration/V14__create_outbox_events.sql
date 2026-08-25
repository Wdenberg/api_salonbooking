CREATE TABLE outbox_events (
                               id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               aggregate_type   VARCHAR(100) NOT NULL,
                               aggregate_id     UUID NOT NULL,
                               event_type       VARCHAR(100) NOT NULL,
                               payload          TEXT NOT NULL,
                               status           VARCHAR(20) NOT NULL,
                               attempts         INTEGER NOT NULL DEFAULT 0,
                               created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
                               processed_at     TIMESTAMPTZ,
                               next_attempt_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
                               CONSTRAINT chk_outbox_status CHECK (status IN ('PENDING','PROCESSING','PUBLISHED','FAILED'))
);

-- Drives the publisher's polling query (Seção 27): find eligible events, oldest first.
CREATE INDEX idx_outbox_status_next_attempt ON outbox_events (status, next_attempt_at);
CREATE INDEX idx_outbox_aggregate ON outbox_events (aggregate_type, aggregate_id);