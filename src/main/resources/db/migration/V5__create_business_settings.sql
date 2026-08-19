CREATE TABLE business_settings (
    business_id                   UUID PRIMARY KEY REFERENCES businesses (id) ON DELETE CASCADE,
    minimum_advance_minutes       INTEGER NOT NULL DEFAULT 60,
    maximum_advance_days          INTEGER NOT NULL DEFAULT 30,
    cancellation_minimum_minutes  INTEGER NOT NULL DEFAULT 120,
    slot_interval_minutes         INTEGER NOT NULL DEFAULT 30,
    created_at                    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at                    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_settings_positive CHECK (
        minimum_advance_minutes >= 0 AND
        maximum_advance_days > 0 AND
        cancellation_minimum_minutes >= 0 AND
        slot_interval_minutes > 0
    )
);