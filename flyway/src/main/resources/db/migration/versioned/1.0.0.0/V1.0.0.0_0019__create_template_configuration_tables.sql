-- ============================================================
-- Template Configuration Tables
-- ============================================================


-- ============================================================
-- 1. TEMPLATE NAME
-- ============================================================

CREATE TABLE template_name
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    template_name VARCHAR(100) NOT NULL
        CHECK (TRIM(template_name) <> ''),
    sheet_name    VARCHAR(100) NOT NULL
        CHECK (TRIM(sheet_name) <> ''),
    db_table_name VARCHAR(200) NOT NULL
        CHECK (TRIM(db_table_name) <> ''),
    created_by    BIGINT       NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by    BIGINT,
    updated_at    TIMESTAMPTZ,

    CONSTRAINT fk_template_name_created_by
        FOREIGN KEY (created_by) REFERENCES users (id),

    CONSTRAINT fk_template_name_updated_by
        FOREIGN KEY (updated_by) REFERENCES users (id),

    CONSTRAINT chk_template_name_updated_status
        CHECK (
            (updated_by IS NULL AND updated_at IS NULL)
                OR
            (updated_by IS NOT NULL AND updated_at IS NOT NULL)
            )
);

CREATE UNIQUE INDEX ux_template_name
    ON template_name (LOWER(TRIM(template_name)));


-- ============================================================
-- 2. TEMPLATE HEADER
-- ============================================================

CREATE TABLE template_header
(
    id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    db_column_name        VARCHAR(100) NOT NULL
        CHECK (TRIM(db_column_name) <> ''),
    template_name_id      BIGINT       NOT NULL,
    header_name           VARCHAR(100) NOT NULL
        CHECK (TRIM(header_name) <> ''),
    column_index          INTEGER      NOT NULL
        CHECK (column_index >= 0),
    data_type             VARCHAR(50)  NOT NULL
        CHECK (
            TRIM(data_type) <> ''
                AND data_type IN ('TEXT', 'NUMBER', 'P_NUMBER', 'DATE', 'AMOUNT')
            ),
    required              BOOLEAN      NOT NULL DEFAULT FALSE,
    min_length            INTEGER
        CHECK (min_length IS NULL OR min_length >= 0),
    max_length            INTEGER
        CHECK (max_length IS NULL OR max_length >= 0),
    regex_pattern         VARCHAR(200),
    db_validation_type    VARCHAR(30)
        CHECK (
            db_validation_type IS NULL
                OR db_validation_type IN ('DB_EXISTS', 'DB_DUPLICATE')
            ),
    db_validation_query   TEXT
        CHECK (
            db_validation_query IS NULL
                OR TRIM(db_validation_query) <> ''
            ),
    db_validation_columns TEXT
        CHECK (
            db_validation_columns IS NULL
                OR TRIM(db_validation_columns) <> ''
            ),
    description           TEXT         NOT NULL
        CHECK (TRIM(description) <> ''),
    created_by            BIGINT       NOT NULL,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by            BIGINT,
    updated_at            TIMESTAMPTZ,

    CONSTRAINT fk_template_header_template_name
        FOREIGN KEY (template_name_id)
            REFERENCES template_name (id),

    CONSTRAINT fk_template_header_created_by
        FOREIGN KEY (created_by) REFERENCES users (id),

    CONSTRAINT fk_template_header_updated_by
        FOREIGN KEY (updated_by) REFERENCES users (id),

    CONSTRAINT chk_template_header_length
        CHECK (
            min_length IS NULL
                OR max_length IS NULL
                OR min_length <= max_length
            ),

    CONSTRAINT chk_template_header_updated_status
        CHECK (
            (updated_by IS NULL AND updated_at IS NULL)
                OR
            (updated_by IS NOT NULL AND updated_at IS NOT NULL)
            )
);

CREATE UNIQUE INDEX ux_template_header
    ON template_header
        (
         template_name_id,
         LOWER(TRIM(header_name))
            );

CREATE UNIQUE INDEX ux_template_header_column_index
    ON template_header (template_name_id, column_index);

CREATE UNIQUE INDEX ux_template_header_db_column_name
    ON template_header
        (
         template_name_id,
         LOWER(TRIM(db_column_name))
            );


-- ============================================================
-- 3. TEMPLATE ADDITIONAL SHEET
-- ============================================================

CREATE TABLE template_additional_sheet
(
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    template_name_id BIGINT       NOT NULL,
    sheet_name       VARCHAR(100) NOT NULL
        CHECK (TRIM(sheet_name) <> ''),
    data_reference   VARCHAR(100) NOT NULL
        CHECK (TRIM(data_reference) <> ''),
    created_by       BIGINT       NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by       BIGINT,
    updated_at       TIMESTAMPTZ,

    CONSTRAINT fk_template_additional_sheet_template_name
        FOREIGN KEY (template_name_id)
            REFERENCES template_name (id),

    CONSTRAINT fk_template_additional_sheet_created_by
        FOREIGN KEY (created_by) REFERENCES users (id),

    CONSTRAINT fk_template_additional_sheet_updated_by
        FOREIGN KEY (updated_by) REFERENCES users (id),

    CONSTRAINT chk_template_additional_sheet_updated_status
        CHECK (
            (updated_by IS NULL AND updated_at IS NULL)
                OR
            (updated_by IS NOT NULL AND updated_at IS NOT NULL)
            )
);

CREATE UNIQUE INDEX ux_template_additional_sheet
    ON template_additional_sheet
        (
         template_name_id,
         LOWER(TRIM(sheet_name))
            );