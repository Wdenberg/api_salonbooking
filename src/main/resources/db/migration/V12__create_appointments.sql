CREATE TABLE appointments (
    id                                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id                       UUID NOT NULL REFERENCES businesses (id),
    customer_id                       UUID NOT NULL REFERENCES users (id),
    employee_id                       UUID NOT NULL REFERENCES employees (id),
    service_id                        UUID NOT NULL REFERENCES services (id),
    start_at                          TIMESTAMPTZ NOT NULL,
    end_at                            TIMESTAMPTZ NOT NULL,
    status                            VARCHAR(20) NOT NULL,
    notes                             VARCHAR(500),

    -- Historical snapshots (Seção 128-131): preserve what was actually agreed at
    -- booking time, independent of later changes to the ServiceOffering or Employee.
    service_name_snapshot             VARCHAR(150) NOT NULL,
    service_price_amount_snapshot     NUMERIC(10,2) NOT NULL,
    service_price_currency_snapshot   VARCHAR(3) NOT NULL,
    service_duration_minutes_snapshot INTEGER NOT NULL,
    employee_name_snapshot            VARCHAR(150) NOT NULL,

    created_at                        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at                        TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT chk_appointments_range CHECK (start_at < end_at),
    CONSTRAINT chk_appointments_status CHECK (status IN ('PENDING','CONFIRMED','CANCELLED','COMPLETED','NO_SHOW'))
);

CREATE INDEX idx_appointments_business_start ON appointments (business_id, start_at);
CREATE INDEX idx_appointments_employee_start ON appointments (employee_id, start_at);
CREATE INDEX idx_appointments_customer_start ON appointments (customer_id, start_at);
CREATE INDEX idx_appointments_status ON appointments (status);

-- =====================================================================================
-- THE critical invariant of the whole system (Seção 24): the database itself refuses
-- to store two overlapping appointments for the same employee, for as long as both
-- are in an "active" status (PENDING or CONFIRMED). CANCELLED/COMPLETED/NO_SHOW
-- appointments no longer occupy the timeline, so they are excluded from the constraint
-- via the WHERE predicate — this allows a new booking to reuse a slot that was freed
-- by a cancellation, without needing to delete historical rows.
--
-- Requires the btree_gist extension (created in V1__init_extensions.sql) so that a
-- plain equality column (employee_id) can be combined with a range operator (&&)
-- inside a single GiST index.
-- =====================================================================================
ALTER TABLE appointments
    ADD CONSTRAINT excl_appointments_employee_overlap
    EXCLUDE USING gist (
        employee_id WITH =,
        tstzrange(start_at, end_at) WITH &&
    )
    WHERE (status IN ('PENDING', 'CONFIRMED'));