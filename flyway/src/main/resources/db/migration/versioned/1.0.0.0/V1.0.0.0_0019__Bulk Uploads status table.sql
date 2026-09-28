CREATE TABLE bulk_uploads_status
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id       BIGINT       NOT NULL REFERENCES users (id),
    upload_type   VARCHAR(100) NOT NULL,
    uploaded_file VARCHAR(100) NOT NULL,
    status        VARCHAR(100) NOT NULL,
    error_file    VARCHAR(120) NOT NULL,
    created_by    BIGINT       NOT NULL REFERENCES users (id),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);