-- =========================================================
-- Create user_categories table
-- =========================================================

CREATE TABLE user_categories
(
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id              BIGINT       NOT NULL,
    transaction_type     BIGINT       NOT NULL,

    category_name        VARCHAR(100) NOT NULL
        CHECK (TRIM(category_name) <> ''),

    category_description TEXT
        CHECK
            (
            category_description IS NULL
                OR TRIM(category_description) <> ''
            ),

    is_active            BOOLEAN      NOT NULL DEFAULT TRUE,

    created_by           BIGINT       NOT NULL,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_by           BIGINT,
    updated_at           TIMESTAMPTZ,

    CONSTRAINT fk_user_categories_user
        FOREIGN KEY (user_id)
            REFERENCES users (id),

    CONSTRAINT fk_user_categories_transaction_type
        FOREIGN KEY (transaction_type, user_id)
            REFERENCES user_reference_value (id, user_id),

    CONSTRAINT fk_user_categories_created_by
        FOREIGN KEY (created_by)
            REFERENCES users (id),

    CONSTRAINT fk_user_categories_updated_by
        FOREIGN KEY (updated_by)
            REFERENCES users (id),

    CONSTRAINT chk_user_categories_updated_status
        CHECK
            (
            (updated_by IS NULL AND updated_at IS NULL)
                OR
            (updated_by IS NOT NULL AND updated_at IS NOT NULL)
            )
);

-- =========================================================
-- Create unique index
-- =========================================================

CREATE UNIQUE INDEX ux_user_categories_user_transaction_type_category_name
    ON user_categories
        (
         user_id,
         transaction_type,
         LOWER(TRIM(category_name))
            );