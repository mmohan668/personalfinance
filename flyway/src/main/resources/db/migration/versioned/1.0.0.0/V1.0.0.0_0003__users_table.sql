-- =========================================================
-- Create users table
-- =========================================================

CREATE TABLE users
(
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    admin_user_id        BIGINT,

    username             VARCHAR(100) NOT NULL
        CHECK (TRIM(username) <> ''),

    display_name         VARCHAR(100) NOT NULL
        CHECK (TRIM(display_name) <> ''),

    email                VARCHAR(255) NOT NULL
        CHECK (TRIM(email) <> ''),

    password_hash        VARCHAR(255) NOT NULL
        CHECK (TRIM(password_hash) <> ''),

    must_change_password BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active            BOOLEAN      NOT NULL DEFAULT FALSE,

    created_by           VARCHAR(100) NOT NULL
        CHECK (TRIM(created_by) <> ''),

    created_at           TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_by           VARCHAR(100),
    updated_at           TIMESTAMPTZ,

    -- Soft delete columns
    is_deleted           BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_by           VARCHAR(100),
    deleted_at           TIMESTAMPTZ,

    -- Constraints
    CONSTRAINT fk_users_admin_user
        FOREIGN KEY (admin_user_id)
            REFERENCES users (id),

    CONSTRAINT chk_users_deleted_status
        CHECK (
            (
                is_deleted = FALSE
                    AND deleted_at IS NULL
                    AND deleted_by IS NULL
                )
                OR
            (
                is_deleted = TRUE
                    AND deleted_at IS NOT NULL
                )
            )
);


-- =========================================================
-- Create users indexes
-- =========================================================

CREATE UNIQUE INDEX ux_users_user_name
    ON users (LOWER(TRIM(username)))
    WHERE is_deleted = FALSE;


CREATE UNIQUE INDEX ux_users_email
    ON users (LOWER(TRIM(email)))
    WHERE is_deleted = FALSE;


CREATE INDEX idx_users_admin_user_id
    ON users (admin_user_id);