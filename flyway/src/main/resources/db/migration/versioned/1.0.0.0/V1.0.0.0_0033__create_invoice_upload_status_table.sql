CREATE TABLE invoice_upload_status
(
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    invoice_number VARCHAR NOT NULL,
    invoice_date   DATE         NOT NULL,
    file_name      VARCHAR NOT NULL,
    status         VARCHAR NOT NULL,
    category_subcategory_suggestion JSONB,
    uploaded_date  DATE         NOT NULL DEFAULT CURRENT_DATE,
    uploaded_by    BIGINT       NOT NULL,
    uploaded_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_invoice_upload_status_uploaded_by
        FOREIGN KEY (uploaded_by) REFERENCES users (id)
);

CREATE UNIQUE INDEX uq_invoice_upload_status_success_invoice_number
    ON invoice_upload_status (LOWER(TRIM(invoice_number)))
    WHERE status = 'Success';
