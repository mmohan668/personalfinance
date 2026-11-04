CREATE TABLE user_subcategories
(
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_category_id        BIGINT       NOT NULL,
    subcategory_name        VARCHAR(100) NOT NULL
        CHECK (TRIM(subcategory_name) <> ''),
    subcategory_description TEXT
        CHECK (
            subcategory_description IS NULL
                OR TRIM(subcategory_description) <> ''
            ),
    is_active               BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by              BIGINT       NOT NULL,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by              BIGINT,
    updated_at              TIMESTAMPTZ,

    CONSTRAINT fk_user_subcategories_user_category
        FOREIGN KEY (user_category_id)
            REFERENCES user_categories (id),

    CONSTRAINT fk_user_subcategories_created_by
        FOREIGN KEY (created_by)
            REFERENCES users (id),

    CONSTRAINT fk_user_subcategories_updated_by
        FOREIGN KEY (updated_by)
            REFERENCES users (id),

    CONSTRAINT chk_user_subcategories_updated_status
        CHECK (
            (updated_by IS NULL AND updated_at IS NULL)
                OR
            (updated_by IS NOT NULL AND updated_at IS NOT NULL)
            )
);


CREATE UNIQUE INDEX ux_user_subcategories_category_subcategory_name
    ON user_subcategories
        (
         user_category_id,
         LOWER(TRIM(subcategory_name))
            );