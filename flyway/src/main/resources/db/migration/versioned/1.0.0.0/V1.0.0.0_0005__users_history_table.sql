CREATE TABLE users_history
(
    history_id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id              BIGINT       NOT NULL,
    admin_user_id        BIGINT,
    username             VARCHAR(100) NOT NULL,
    display_name         VARCHAR(100) NOT NULL,
    email                VARCHAR(255),
    must_change_password BOOLEAN      NOT NULL,
    is_active            BOOLEAN      NOT NULL,
    created_by           VARCHAR(100) NOT NULL,
    created_at           TIMESTAMPTZ  NOT NULL,
    updated_by           VARCHAR(100),
    updated_at           TIMESTAMPTZ,
    is_deleted           BOOLEAN      NOT NULL,
    deleted_by           VARCHAR(100),
    deleted_at           TIMESTAMPTZ,
    action_type          VARCHAR(10)  NOT NULL CHECK (action_type IN ('INSERT', 'UPDATE', 'DELETE')),
    action_by            VARCHAR(100) NOT NULL,
    action_at            TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_users_history_user
        FOREIGN KEY (user_id)
            REFERENCES users (id),

    CONSTRAINT fk_users_history_admin_user
        FOREIGN KEY (admin_user_id)
            REFERENCES users (id),

    CONSTRAINT chk_users_history_deleted_status
        CHECK (
            (is_deleted = FALSE AND deleted_at IS NULL AND deleted_by IS NULL)
                OR
            (is_deleted = TRUE AND deleted_at IS NOT NULL)
            )
);

CREATE INDEX idx_users_history_user_id_action_at
    ON users_history (user_id, action_at DESC);

CREATE INDEX idx_users_history_action_at
    ON users_history (action_at DESC);

CREATE INDEX idx_users_history_admin_user_id
    ON users_history (admin_user_id);
