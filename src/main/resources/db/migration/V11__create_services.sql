CREATE TABLE services (
                          id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          business_id    UUID NOT NULL REFERENCES businesses (id) ON DELETE CASCADE,
                          name           VARCHAR(150) NOT NULL,
                          description    VARCHAR(1000),
                          price_amount   NUMERIC(10,2) NOT NULL,
                          price_currency VARCHAR(3) NOT NULL,
                          duration_minutes INTEGER NOT NULL,
                          active         BOOLEAN NOT NULL DEFAULT true,
                          created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
                          updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
                          CONSTRAINT chk_services_price_positive CHECK (price_amount >= 0),
                          CONSTRAINT chk_services_duration_positive CHECK (duration_minutes > 0)
);

CREATE INDEX idx_services_business_active ON services (business_id, active);