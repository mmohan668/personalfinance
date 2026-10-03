CREATE TABLE bulk_uploads_status
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id       BIGINT       NOT NULL REFERENCES users (id),
    upload_type   VARCHAR(100) NOT NULL,
    template_name BIGINT       NOT NULL REFERENCES template_name (id),
    uploaded_file TEXT         NOT NULL,
    status        VARCHAR(100) NOT NULL,
    error_file    TEXT,
    remarks       TEXT,
    created_by    BIGINT       NOT NULL REFERENCES users (id),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);