CREATE TABLE categories
(
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    transaction_type     BIGINT       NOT NULL,
    category_name        VARCHAR(100) NOT NULL CHECK ( TRIM(category_name) <> '' ),
    category_description TEXT,
    is_active            BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by           BIGINT       NOT NULL REFERENCES users (id),
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by           BIGINT REFERENCES users (id),
    updated_at           TIMESTAMPTZ,

    CONSTRAINT fk_transaction_type
        FOREIGN KEY (transaction_type)
            REFERENCES reference_value (id)
);

CREATE UNIQUE INDEX ux_category
    ON categories (transaction_type, LOWER(TRIM(category_name)));

CREATE TABLE subcategories
(
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    category_id             BIGINT       NOT NULL,
    subcategory_name        VARCHAR(100) NOT NULL CHECK (TRIM(subcategory_name) <> ''),
    subcategory_description TEXT,
    is_active               BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by              BIGINT       NOT NULL REFERENCES users (id),
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by              BIGINT REFERENCES users (id),
    updated_at              TIMESTAMPTZ,

    CONSTRAINT fk_subcategories_category
        FOREIGN KEY (category_id)
            REFERENCES categories (id)
);

CREATE UNIQUE INDEX ux_category_subcategory
    ON subcategories (category_id, LOWER(TRIM(subcategory_name)));

CREATE TABLE user_categories
(
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id              BIGINT       NOT NULL,
    transaction_type     BIGINT       NOT NULL,
    category_name        VARCHAR(100) NOT NULL CHECK (TRIM(category_name) <> ''),
    category_description TEXT,
    is_active            BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by           BIGINT       NOT NULL REFERENCES users (id),
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by           BIGINT REFERENCES users (id),
    updated_at           TIMESTAMPTZ,

    CONSTRAINT fk_user_categories_user
        FOREIGN KEY (user_id)
            REFERENCES users (id),
    CONSTRAINT fk_user_transaction_type
        FOREIGN KEY (transaction_type)
            REFERENCES reference_value (id)
);

CREATE UNIQUE INDEX ux_user_category
    ON user_categories (user_id, transaction_type, LOWER(TRIM(category_name)));

CREATE TABLE user_subcategories
(
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_category_id        BIGINT       NOT NULL,
    subcategory_name        VARCHAR(100) NOT NULL CHECK (TRIM(subcategory_name) <> ''),
    subcategory_description TEXT,
    is_active               BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by              BIGINT       NOT NULL REFERENCES users (id),
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by              BIGINT REFERENCES users (id),
    updated_at              TIMESTAMPTZ,

    CONSTRAINT fk_user_subcategories_user_category
        FOREIGN KEY (user_category_id)
            REFERENCES user_categories (id)
);

CREATE UNIQUE INDEX ux_user_category_subcategory
    ON user_subcategories (user_category_id, LOWER(TRIM(subcategory_name)));