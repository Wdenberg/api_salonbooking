CREATE TABLE employees (
                           id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           user_id      UUID NOT NULL REFERENCES users (id),
                           business_id  UUID NOT NULL REFERENCES businesses (id) ON DELETE CASCADE,
                           specialty    VARCHAR(150),
                           status       VARCHAR(20) NOT NULL,
                           created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
                           updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
                           CONSTRAINT uq_employees_user_business UNIQUE (user_id, business_id),
                           CONSTRAINT chk_employees_status CHECK (status IN ('ACTIVE','INACTIVE','ON_LEAVE'))
);

CREATE INDEX idx_employees_business_status ON employees (business_id, status);
CREATE INDEX idx_employees_user_id ON employees (user_id);