CREATE TABLE refresh_tokens (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id              UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash           VARCHAR(255) NOT NULL,
    parent_token_hash    VARCHAR(255),  -- for rotation: hash of the previous refresh token
    revoked              BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at           TIMESTAMPTZ,
    revoked_reason       VARCHAR(100),  -- 'LOGOUT', 'ROTATION_REUSE_DETECTED', 'ADMIN_REVOKE'
    expires_at           TIMESTAMPTZ NOT NULL,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_refresh_tokens_token_hash UNIQUE (token_hash)
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at) WHERE revoked = FALSE;

-- Table for access token blocklist (for immediate revocation on logout)
CREATE TABLE access_token_blocklist (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token_jti       VARCHAR(255) NOT NULL,
    expires_at      TIMESTAMPTZ NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_access_token_blocklist_jti UNIQUE (token_jti)
);

CREATE INDEX idx_access_token_blocklist_expires_at ON access_token_blocklist (expires_at);