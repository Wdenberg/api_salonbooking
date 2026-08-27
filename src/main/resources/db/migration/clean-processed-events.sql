DROP TABLE IF EXISTS processed_events CASCADE;
CREATE TABLE processed_events (
                                  event_id     UUID NOT NULL,
                                  consumer     VARCHAR(150) NOT NULL,
                                  processed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                  PRIMARY KEY (event_id, consumer)
);