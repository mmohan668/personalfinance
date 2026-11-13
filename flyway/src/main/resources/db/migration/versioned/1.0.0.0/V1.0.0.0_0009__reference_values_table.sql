CREATE TABLE reference_values
(
    id                         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ref_obj_name_id            BIGINT       NOT NULL,
    reference_code             VARCHAR(100) NOT NULL CHECK (TRIM(reference_code) <> ''),
    reference_code_description VARCHAR(200) NOT NULL CHECK (TRIM(reference_code_description) <> ''),
    reference_code_2           VARCHAR(100) CHECK (reference_code_2 IS NULL OR TRIM(reference_code_2) <> ''),
    reference_code_3           VARCHAR(100) CHECK (reference_code_3 IS NULL OR TRIM(reference_code_3) <> ''),
    created_by                 BIGINT       NOT NULL,
    created_at                 TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by                 BIGINT,
    updated_at                 TIMESTAMPTZ,

    CONSTRAINT fk_reference_values_ref_object
        FOREIGN KEY (ref_obj_name_id)
            REFERENCES reference_object (id),

    CONSTRAINT fk_reference_values_created_by
        FOREIGN KEY (created_by)
            REFERENCES users (id),

    CONSTRAINT fk_reference_values_updated_by
        FOREIGN KEY (updated_by)
            REFERENCES users (id),

    CONSTRAINT chk_reference_values_updated_status
        CHECK (
            (updated_by IS NULL AND updated_at IS NULL)
                OR
            (updated_by IS NOT NULL AND updated_at IS NOT NULL)
            )
);

CREATE UNIQUE INDEX ux_reference_values_object_code
    ON reference_values
        (ref_obj_name_id, LOWER(TRIM(reference_code)));