-- =========================================================
-- Create user permissions table
-- =========================================================

CREATE UNIQUE INDEX ux_app_screen_id_module
    ON app_screen (id, module_id);

CREATE UNIQUE INDEX ux_app_action_id_screen
    ON app_action (id, screen_id);

CREATE TABLE user_permissions
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id    BIGINT      NOT NULL,
    module_id  BIGINT      NOT NULL,
    screen_id  BIGINT      NOT NULL,
    action_id  BIGINT      NOT NULL,
    has_permission BOOLEAN NOT NULL DEFAULT FALSE,

    created_by BIGINT      NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ,

    CONSTRAINT fk_user_permissions_user
        FOREIGN KEY (user_id)
            REFERENCES users (id),

    CONSTRAINT fk_user_permissions_screen_module
        FOREIGN KEY (screen_id, module_id)
            REFERENCES app_screen (id, module_id),

    CONSTRAINT fk_user_permissions_action_screen
        FOREIGN KEY (action_id, screen_id)
            REFERENCES app_action (id, screen_id),

    CONSTRAINT fk_user_permissions_created_by
        FOREIGN KEY (created_by)
            REFERENCES users (id),

    CONSTRAINT fk_user_permissions_updated_by
        FOREIGN KEY (updated_by)
            REFERENCES users (id),

    CONSTRAINT chk_user_permissions_updated_status
        CHECK (
            (updated_by IS NULL AND updated_at IS NULL)
                OR
            (updated_by IS NOT NULL AND updated_at IS NOT NULL)
            )
);

-- =========================================================
-- Create indexes
-- =========================================================

CREATE UNIQUE INDEX ux_user_permissions_user_action
    ON user_permissions (user_id, action_id);

CREATE INDEX idx_user_permissions_module_screen
    ON user_permissions (module_id, screen_id);
