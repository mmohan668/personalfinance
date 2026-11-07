-- =========================================================
-- Create application module, screen, and action catalogs
-- =========================================================

CREATE TABLE app_module
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    module_name  VARCHAR(150) NOT NULL
        CHECK (TRIM(module_name) <> ''),

    description  TEXT
        CHECK (description IS NULL OR TRIM(description) <> ''),

    display_order INTEGER     NOT NULL DEFAULT 0,
    is_active     BOOLEAN     NOT NULL DEFAULT TRUE,

    created_by    BIGINT      NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by    BIGINT,
    updated_at    TIMESTAMPTZ,

    CONSTRAINT fk_app_module_created_by
        FOREIGN KEY (created_by)
            REFERENCES users (id),

    CONSTRAINT fk_app_module_updated_by
        FOREIGN KEY (updated_by)
            REFERENCES users (id),

    CONSTRAINT chk_app_module_updated_status
        CHECK (
            (updated_by IS NULL AND updated_at IS NULL)
                OR
            (updated_by IS NOT NULL AND updated_at IS NOT NULL)
            )
);

CREATE TABLE app_screen
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    module_id    BIGINT       NOT NULL,
    screen_name  VARCHAR(150) NOT NULL
        CHECK (TRIM(screen_name) <> ''),

    route_path   VARCHAR(500)
        CHECK (route_path IS NULL OR TRIM(route_path) <> ''),

    description  TEXT
        CHECK (description IS NULL OR TRIM(description) <> ''),

    display_order INTEGER     NOT NULL DEFAULT 0,
    is_active     BOOLEAN     NOT NULL DEFAULT TRUE,

    created_by    BIGINT      NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by    BIGINT,
    updated_at    TIMESTAMPTZ,

    CONSTRAINT fk_app_screen_module
        FOREIGN KEY (module_id)
            REFERENCES app_module (id),

    CONSTRAINT fk_app_screen_created_by
        FOREIGN KEY (created_by)
            REFERENCES users (id),

    CONSTRAINT fk_app_screen_updated_by
        FOREIGN KEY (updated_by)
            REFERENCES users (id),

    CONSTRAINT chk_app_screen_updated_status
        CHECK (
            (updated_by IS NULL AND updated_at IS NULL)
                OR
            (updated_by IS NOT NULL AND updated_at IS NOT NULL)
            )
);

CREATE TABLE app_action
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    screen_id    BIGINT       NOT NULL,
    action_name  VARCHAR(150) NOT NULL
        CHECK (TRIM(action_name) <> ''),

    description  TEXT
        CHECK (description IS NULL OR TRIM(description) <> ''),

    display_order INTEGER     NOT NULL DEFAULT 0,
    is_active     BOOLEAN     NOT NULL DEFAULT TRUE,

    created_by    BIGINT      NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by    BIGINT,
    updated_at    TIMESTAMPTZ,

    CONSTRAINT fk_app_action_screen
        FOREIGN KEY (screen_id)
            REFERENCES app_screen (id),

    CONSTRAINT fk_app_action_created_by
        FOREIGN KEY (created_by)
            REFERENCES users (id),

    CONSTRAINT fk_app_action_updated_by
        FOREIGN KEY (updated_by)
            REFERENCES users (id),

    CONSTRAINT chk_app_action_updated_status
        CHECK (
            (updated_by IS NULL AND updated_at IS NULL)
                OR
            (updated_by IS NOT NULL AND updated_at IS NOT NULL)
            )
);

