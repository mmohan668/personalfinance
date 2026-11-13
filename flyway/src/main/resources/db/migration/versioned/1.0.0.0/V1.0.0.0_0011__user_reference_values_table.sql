CREATE TABLE user_reference_value
(
    id                         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id                    BIGINT       NOT NULL,
    ref_obj_name_id            BIGINT       NOT NULL,
    reference_code             VARCHAR(100) NOT NULL CHECK (TRIM(reference_code) <> ''),
    reference_code_description VARCHAR(200) NOT NULL CHECK (TRIM(reference_code_description) <> ''),
    is_active                  BOOLEAN      NOT NULL DEFAULT TRUE,
    reference_code_2           VARCHAR(100) CHECK ( reference_code_2 IS NULL OR TRIM(reference_code_2) <> '' ),
    reference_code_3           VARCHAR(100) CHECK ( reference_code_3 IS NULL OR TRIM(reference_code_3) <> '' ),
    created_by                 BIGINT       NOT NULL,
    created_at                 TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by                 BIGINT,
    updated_at                 TIMESTAMPTZ,
    is_deleted                 BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_by                 VARCHAR(100),
    deleted_at                 TIMESTAMPTZ,

    CONSTRAINT fk_user_reference_value_user
        FOREIGN KEY (user_id)
            REFERENCES users (id),

    CONSTRAINT fk_user_reference_value_ref_object
        FOREIGN KEY (ref_obj_name_id)
            REFERENCES reference_object (id),

    CONSTRAINT fk_user_reference_value_created_by
        FOREIGN KEY (created_by)
            REFERENCES users (id),

    CONSTRAINT fk_user_reference_value_updated_by
        FOREIGN KEY (updated_by)
            REFERENCES users (id),

    CONSTRAINT chk_user_reference_value_updated_status
        CHECK (
            (updated_by IS NULL AND updated_at IS NULL)
                OR
            (updated_by IS NOT NULL AND updated_at IS NOT NULL)
            ),

    CONSTRAINT uq_user_reference_value_id_user
        UNIQUE (id, user_id),

    CONSTRAINT chk_user_reference_values_deleted_status
        CHECK (
            (is_deleted = FALSE AND deleted_at IS NULL AND deleted_by IS NULL)
                OR
            (is_deleted = TRUE AND deleted_at IS NOT NULL)
            )
);

CREATE UNIQUE INDEX ux_user_reference_value_object_code_user
    ON user_reference_value (ref_obj_name_id, LOWER(TRIM(reference_code)), user_id);