CREATE TABLE users (
                       id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       name           VARCHAR(150) NOT NULL,
                       email          VARCHAR(255) NOT NULL,
                       password_hash  VARCHAR(255) NOT NULL,
                       status         VARCHAR(20)  NOT NULL,
                       created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
                       updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
                       CONSTRAINT uq_users_email UNIQUE (email),
                       CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE','INACTIVE','BLOCKED'))
);

CREATE INDEX idx_users_email ON users (email);