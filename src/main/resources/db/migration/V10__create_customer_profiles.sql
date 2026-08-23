CREATE TABLE customer_profiles (
                                   user_id        UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
                                   phone          VARCHAR(30),
                                   date_of_birth  DATE,
                                   created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
                                   updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);