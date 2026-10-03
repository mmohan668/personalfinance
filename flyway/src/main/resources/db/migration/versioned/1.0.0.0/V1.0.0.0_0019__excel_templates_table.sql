CREATE TABLE template_name
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    template_name VARCHAR(100) NOT NULL CHECK ( TRIM(template_name) <> '' ),
    sheet_name    VARCHAR(100) NOT NULL CHECK ( TRIM(sheet_name) <> '' ),
    created_by    BIGINT       NOT NULL REFERENCES users (id),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT current_timestamp,
    updated_by    BIGINT REFERENCES users (id),
    updated_at    TIMESTAMPTZ
);

CREATE UNIQUE INDEX ux_template_name
    ON template_name (LOWER(TRIM(template_name)));

CREATE TABLE template_header
(
    id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    template_name_id      BIGINT       NOT NULL REFERENCES template_name (id),
    header_name           VARCHAR(100) NOT NULL CHECK ( TRIM(header_name) <> '' ),
    column_index          INTEGER      NOT NULL CHECK ( column_index >= 0 ),
    data_type             VARCHAR(50)  NOT NULL
        CHECK ( TRIM(data_type) <> '' AND data_type IN ('TEXT', 'NUMBER', 'P_NUMBER', 'DATE', 'AMOUNT')),
    required              BOOLEAN      NOT NULL DEFAULT FALSE,
    min_length            INTEGER CHECK ( min_length IS NULL OR min_length >= 0 ),
    max_length            INTEGER CHECK ( max_length IS NULL OR max_length >= 0 ),
    CHECK (
        min_length IS NULL
            OR max_length IS NULL
            OR min_length <= max_length
        ),
    regex_pattern         VARCHAR(200),
    db_validation_type    VARCHAR
        CHECK ( TRIM(db_validation_type) <> '' AND db_validation_type IN ('DB_EXISTS', 'DB_DUPLICATE') ),
    db_validation_query   TEXT CHECK ( TRIM(db_validation_query) <> '' ),
    db_validation_columns TEXT CHECK ( TRIM(db_validation_columns) <> '' ),
    description           TEXT         NOT NULL CHECK ( TRIM(description) <> '' ),
    created_by            BIGINT       NOT NULL REFERENCES users (id),
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT current_timestamp,
    updated_by            BIGINT REFERENCES users (id),
    updated_at            TIMESTAMPTZ
);

CREATE UNIQUE INDEX ux_template_header
    ON template_header (template_name_id, LOWER(TRIM(header_name)));

CREATE UNIQUE INDEX ux_template_header_column_index
    ON template_header (template_name_id, column_index);

CREATE TABLE template_additional_sheet
(
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    template_name_id BIGINT       NOT NULL REFERENCES template_name (id),
    sheet_name       VARCHAR(100) NOT NULL CHECK ( TRIM(sheet_name) <> '' ),
    data_reference   VARCHAR(100) NOT NULL CHECK ( TRIM(data_reference) <> '' ),
    created_by       BIGINT       NOT NULL REFERENCES users (id),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT current_timestamp,
    updated_by       BIGINT REFERENCES users (id),
    updated_at       TIMESTAMPTZ
);

CREATE UNIQUE INDEX ux_template_additional_sheet
    ON template_additional_sheet (template_name_id, LOWER(TRIM(sheet_name)));