-- =========================================================
-- Create reference object table
-- =========================================================

CREATE TABLE reference_object
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    ref_obj_name VARCHAR(100) NOT NULL
        CHECK (TRIM(ref_obj_name) <> ''),

    created_by   BIGINT       NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_by   BIGINT,
    updated_at   TIMESTAMPTZ,

    CONSTRAINT fk_reference_object_created_by
        FOREIGN KEY (created_by)
            REFERENCES users (id),

    CONSTRAINT fk_reference_object_updated_by
        FOREIGN KEY (updated_by)
            REFERENCES users (id),

    CONSTRAINT chk_reference_object_updated_status
        CHECK
            (
            (updated_by IS NULL AND updated_at IS NULL)
                OR
            (updated_by IS NOT NULL AND updated_at IS NOT NULL)
            )
);

-- =========================================================
-- Create indexes
-- =========================================================

CREATE UNIQUE INDEX ux_reference_object_name
    ON reference_object (LOWER(TRIM(ref_obj_name)));