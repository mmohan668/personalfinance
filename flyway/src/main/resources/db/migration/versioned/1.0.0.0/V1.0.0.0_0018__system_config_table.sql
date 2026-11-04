CREATE TABLE system_config
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    config_name  VARCHAR(100)  NOT NULL
        CHECK (TRIM(config_name) <> ''),
    config_value BIGINT        NOT NULL,
    description  VARCHAR(1000) NOT NULL
        CHECK (TRIM(description) <> ''),
    user_id      BIGINT        NOT NULL,
    created_by   BIGINT        NOT NULL,
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by   BIGINT,
    updated_at   TIMESTAMPTZ,

    CONSTRAINT fk_system_config_value_user
        FOREIGN KEY (config_value, user_id)
            REFERENCES user_reference_value (id, user_id),

    CONSTRAINT fk_system_config_user
        FOREIGN KEY (user_id)
            REFERENCES users (id),

    CONSTRAINT fk_system_config_created_by
        FOREIGN KEY (created_by)
            REFERENCES users (id),

    CONSTRAINT fk_system_config_updated_by
        FOREIGN KEY (updated_by)
            REFERENCES users (id),

    CONSTRAINT chk_system_config_updated_status
        CHECK (
            (updated_by IS NULL AND updated_at IS NULL)
                OR
            (updated_by IS NOT NULL AND updated_at IS NOT NULL)
            )
);


CREATE UNIQUE INDEX ux_system_config_name_user
    ON system_config
        (
         LOWER(TRIM(config_name)),
         user_id
            );

-- Supports the composite FK and user-specific reference-value lookups.
CREATE INDEX idx_system_config_config_value_user
    ON system_config (config_value, user_id);
