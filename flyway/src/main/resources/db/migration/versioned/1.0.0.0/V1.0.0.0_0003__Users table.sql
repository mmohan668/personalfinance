CREATE TABLE users
(
    id            BIGSERIAL PRIMARY KEY,
    admin_user_id BIGINT,
    username      VARCHAR(100) NOT NULL CHECK (TRIM(username) <> ''),
    email         VARCHAR(255) NOT NULL CHECK (TRIM(email) <> ''),
    password_hash VARCHAR(255) NOT NULL CHECK (TRIM(password_hash) <> ''),
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by    VARCHAR(100) NOT NULL CHECK ( TRIM(created_by) <> '' ),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by    VARCHAR(100),
    updated_at    TIMESTAMPTZ,
    CONSTRAINT uq_users_username
        UNIQUE (username),

    CONSTRAINT fk_users_admin_user
        FOREIGN KEY (admin_user_id)
            REFERENCES users (id)
);

CREATE UNIQUE INDEX uq_users_email_lower
    ON users (LOWER(email));

CREATE INDEX idx_users_admin_user_id
    ON users (admin_user_id);