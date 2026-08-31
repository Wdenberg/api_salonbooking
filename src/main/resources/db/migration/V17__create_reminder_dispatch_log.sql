CREATE TABLE reminder_dispatch_log (
                                       appointment_id  UUID NOT NULL REFERENCES appointments (id) ON DELETE CASCADE,
                                       reminder_type   VARCHAR(10) NOT NULL,
                                       dispatched_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
                                       PRIMARY KEY (appointment_id, reminder_type),
                                       CONSTRAINT chk_reminder_type CHECK (reminder_type IN ('H24', 'H2'))
);