CREATE TABLE categories
(
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    transaction_type     BIGINT       NOT NULL,
    category_name        VARCHAR(100) NOT NULL CHECK (TRIM(category_name) <> ''),
    category_description TEXT CHECK ( category_description IS NULL OR TRIM(category_description) <> '' ),
    is_active            BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by           BIGINT       NOT NULL,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by           BIGINT,
    updated_at           TIMESTAMPTZ,

    CONSTRAINT fk_categories_transaction_type
        FOREIGN KEY (transaction_type)
            REFERENCES reference_values (id),

    CONSTRAINT fk_categories_created_by
        FOREIGN KEY (created_by)
            REFERENCES users (id),

    CONSTRAINT fk_categories_updated_by
        FOREIGN KEY (updated_by)
            REFERENCES users (id),

    CONSTRAINT chk_categories_updated_status
        CHECK (
            (updated_by IS NULL AND updated_at IS NULL)
                OR
            (updated_by IS NOT NULL AND updated_at IS NOT NULL)
            )
);

CREATE UNIQUE INDEX ux_categories_transaction_type_category_name
    ON categories (transaction_type, LOWER(TRIM(category_name)));