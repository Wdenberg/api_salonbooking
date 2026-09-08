CREATE TABLE audit_events (
                              id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              actor_user_id  UUID NOT NULL REFERENCES users (id),
                              business_id    UUID REFERENCES businesses (id),
                              action         VARCHAR(50) NOT NULL,
                              resource_type  VARCHAR(50) NOT NULL,
                              resource_id    UUID NOT NULL,
                              metadata       TEXT,
                              ip_address     VARCHAR(45),
                              user_agent     VARCHAR(500),
                              occurred_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_events_business ON audit_events (business_id, occurred_at);
CREATE INDEX idx_audit_events_actor ON audit_events (actor_user_id, occurred_at);
CREATE INDEX idx_audit_events_resource ON audit_events (resource_type, resource_id);