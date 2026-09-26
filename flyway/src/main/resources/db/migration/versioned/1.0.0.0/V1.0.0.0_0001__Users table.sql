CREATE TABLE users
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    admin_user_id BIGINT,
    username      VARCHAR(100) NOT NULL CHECK (TRIM(username) <> ''),
    display_name  VARCHAR(100) NOT NULL CHECK (TRIM(display_name) <> ''),
    email         VARCHAR(255) NOT NULL CHECK (TRIM(email) <> ''),
    password_hash VARCHAR(255) NOT NULL CHECK (TRIM(password_hash) <> ''),
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by    VARCHAR(100) NOT NULL CHECK (TRIM(created_by) <> ''),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by    VARCHAR(100),
    updated_at    TIMESTAMPTZ,

    CONSTRAINT fk_users_admin_user
        FOREIGN KEY (admin_user_id)
            REFERENCES users (id)
);

CREATE UNIQUE INDEX ux_users_user_name
    ON users (LOWER(TRIM(username)));

CREATE UNIQUE INDEX ux_users_email
    ON users (LOWER(TRIM(email)));

CREATE INDEX idx_users_admin_user_id
    ON users (admin_user_id);

INSERT INTO users(username,
                  display_name,
                  email,
                  password_hash,
                  created_by)
VALUES ('PERSONALFINANCEAPP',
        'PERSONAL FINANCE APP',
        'personalfinanceapp.support@gmail.com',
        'PASSWORD_HASH_TEMP',
        'FLYWAY')