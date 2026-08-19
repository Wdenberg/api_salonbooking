CREATE TABLE businesses (
                            id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            owner_id          UUID NOT NULL REFERENCES users (id),
                            name              VARCHAR(150) NOT NULL,
                            description       VARCHAR(1000),
                            phone             VARCHAR(30),
                            email             VARCHAR(255),
                            address_street    VARCHAR(200),
                            address_number    VARCHAR(20),
                            address_city      VARCHAR(100),
                            address_state     VARCHAR(100),
                            address_zip_code  VARCHAR(20),
                            address_country   VARCHAR(100),
                            timezone          VARCHAR(50) NOT NULL,
                            status            VARCHAR(20) NOT NULL,
                            created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                            updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                            CONSTRAINT chk_businesses_status CHECK (status IN ('ACTIVE','INACTIVE','SUSPENDED'))
);

CREATE INDEX idx_businesses_owner_id ON businesses (owner_id);