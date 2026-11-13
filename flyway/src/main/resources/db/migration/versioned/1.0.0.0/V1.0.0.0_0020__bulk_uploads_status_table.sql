CREATE TABLE bulk_uploads_status
(
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id            BIGINT       NOT NULL,
    upload_type        VARCHAR(100) NOT NULL CHECK (TRIM(upload_type) <> ''),
    template_name      BIGINT       NOT NULL,
    uploaded_file      TEXT         NOT NULL CHECK (TRIM(uploaded_file) <> ''),
    status             VARCHAR(100) NOT NULL CHECK (TRIM(status) <> ''),
    error_file         TEXT CHECK (error_file IS NULL OR TRIM(error_file) <> ''),
    remarks            TEXT CHECK (remarks IS NULL OR TRIM(remarks) <> ''),
    started_at         TIMESTAMPTZ,
    completed_at       TIMESTAMPTZ,
    total_records      BIGINT       NOT NULL DEFAULT 0 CHECK (total_records >= 0),
    successful_records BIGINT       NOT NULL DEFAULT 0 CHECK (successful_records >= 0),
    failed_records     BIGINT       NOT NULL DEFAULT 0 CHECK (failed_records >= 0),
    created_by         BIGINT       NOT NULL,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_bulk_uploads_status_user
        FOREIGN KEY (user_id)
            REFERENCES users (id),

    CONSTRAINT fk_bulk_uploads_status_template
        FOREIGN KEY (template_name)
            REFERENCES template_name (id),

    CONSTRAINT fk_bulk_uploads_status_created_by
        FOREIGN KEY (created_by)
            REFERENCES users (id),

    CONSTRAINT chk_bulk_uploads_status_completed_at
        CHECK ( completed_at IS NULL OR (started_at IS NOT NULL AND completed_at >= started_at) ),

    CONSTRAINT chk_bulk_uploads_status_record_counts
        CHECK ( successful_records + failed_records <= total_records )
);

CREATE INDEX idx_bulk_uploads_status_template_name
    ON bulk_uploads_status (template_name);

CREATE INDEX idx_bulk_uploads_status_created_at
    ON bulk_uploads_status (created_at DESC);

CREATE INDEX idx_bulk_uploads_status_user_created_at
    ON bulk_uploads_status (user_id, created_at DESC);