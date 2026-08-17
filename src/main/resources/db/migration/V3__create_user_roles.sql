CREATE TABLE user_roles (
                            user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
                            role    VARCHAR(30) NOT NULL,
                            PRIMARY KEY (user_id, role),
                            CONSTRAINT chk_user_roles_role CHECK (role IN ('PLATFORM_ADMIN','OWNER','EMPLOYEE','CUSTOMER'))
);

CREATE INDEX idx_user_roles_user_id ON user_roles (user_id);