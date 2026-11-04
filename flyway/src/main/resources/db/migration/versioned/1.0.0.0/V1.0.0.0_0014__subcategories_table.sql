-- =========================================================
-- Create subcategories table
-- =========================================================

CREATE TABLE subcategories
(
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    category_id             BIGINT       NOT NULL,

    subcategory_name        VARCHAR(100) NOT NULL
        CHECK (TRIM(subcategory_name) <> ''),

    subcategory_description TEXT
        CHECK
            (
            subcategory_description IS NULL
                OR TRIM(subcategory_description) <> ''
            ),

    is_active               BOOLEAN      NOT NULL DEFAULT TRUE,

    created_by              BIGINT       NOT NULL,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_by              BIGINT,
    updated_at              TIMESTAMPTZ,

    CONSTRAINT fk_subcategories_category
        FOREIGN KEY (category_id)
            REFERENCES categories (id),

    CONSTRAINT fk_subcategories_created_by
        FOREIGN KEY (created_by)
            REFERENCES users (id),

    CONSTRAINT fk_subcategories_updated_by
        FOREIGN KEY (updated_by)
            REFERENCES users (id),

    CONSTRAINT chk_subcategories_updated_status
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

CREATE UNIQUE INDEX ux_subcategories_category_subcategory_name
    ON subcategories
        (
         category_id,
         LOWER(TRIM(subcategory_name))
            );