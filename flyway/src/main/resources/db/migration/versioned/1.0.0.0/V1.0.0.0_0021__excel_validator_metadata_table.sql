CREATE TABLE excel_validator_metadata
(
    id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    upload_type           VARCHAR(100) NOT NULL,
    sheet_name            VARCHAR(100) NOT NULL,
    column_name           VARCHAR(100) NOT NULL,
    column_index          INTEGER      NOT NULL CHECK ( column_index >= 0 ),
    data_type             VARCHAR(20)  NOT NULL DEFAULT 'TEXT',
    required              BOOLEAN      NOT NULL DEFAULT false,
    min_length            INTEGER
        CHECK ( min_length IS NULL OR
                (min_length >= 0
                    AND (max_length IS NULL OR min_length <= max_length)
                    )
            ),
    max_length            INTEGER CHECK ( max_length IS NULL OR max_length >= 0 ),
    regex_pattern         TEXT,
    db_validation_type    VARCHAR(50),
    db_validation_query   TEXT,
    db_validation_columns TEXT,
    created_by            BIGINT       NOT NULL REFERENCES users (id),
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT current_timestamp,
    updated_by            BIGINT REFERENCES users (id),
    updated_at            TIMESTAMPTZ
);

CREATE UNIQUE INDEX ux_excel_validator_metadata
    ON excel_validator_metadata
        (
         LOWER(TRIM(upload_type)),
         LOWER(TRIM(sheet_name)),
         LOWER(TRIM(column_name))
            );