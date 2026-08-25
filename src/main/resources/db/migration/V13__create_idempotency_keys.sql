CREATE TABLE idempotency_keys (
                                  id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  idempotency_key       VARCHAR(255) NOT NULL,
                                  user_id               UUID NOT NULL REFERENCES users (id),
                                  endpoint              VARCHAR(200) NOT NULL,
                                  request_fingerprint   VARCHAR(64) NOT NULL,
                                  status                VARCHAR(20) NOT NULL,
                                  response_status       INTEGER,
                                  response_body         TEXT,
                                  created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
                                  completed_at          TIMESTAMPTZ,
                                  CONSTRAINT chk_idempotency_status CHECK (status IN ('IN_PROGRESS','COMPLETED'))
);

-- The single invariant that makes idempotency actually work under concurrency (Seção 73):
-- the database refuses a second IN_PROGRESS row for the same (key, user, endpoint) triple,
-- regardless of how many application instances race to insert it simultaneously.
CREATE UNIQUE INDEX uq_idempotency_key_user_endpoint ON idempotency_keys (idempotency_key, user_id, endpoint);

CREATE INDEX idx_idempotency_created_at ON idempotency_keys (created_at);