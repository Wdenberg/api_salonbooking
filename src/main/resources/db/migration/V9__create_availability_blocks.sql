CREATE TABLE availability_blocks (
                                     id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                     employee_id  UUID NOT NULL REFERENCES employees (id) ON DELETE CASCADE,
                                     start_at     TIMESTAMPTZ NOT NULL,
                                     end_at       TIMESTAMPTZ NOT NULL,
                                     reason       VARCHAR(200),
                                     created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
                                     CONSTRAINT chk_availability_blocks_range CHECK (start_at < end_at)
);

CREATE INDEX idx_availability_blocks_employee_range ON availability_blocks (employee_id, start_at, end_at);