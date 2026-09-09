CREATE TABLE categories
(
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    category_type        BIGINT       NOT NULL,
    category_name        VARCHAR(100) NOT NULL
        CHECK ( TRIM(category_name) <> '' ),
    category_description TEXT,
    is_active            BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by           VARCHAR(100) NOT NULL
        CHECK ( TRIM(created_by) <> '' ),
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by           VARCHAR(100),
    updated_at           TIMESTAMPTZ,

    CONSTRAINT fk_category_type
        FOREIGN KEY (category_type)
            REFERENCES reference_object (id)
);

CREATE UNIQUE INDEX ux_category
    ON categories (category_type, LOWER(TRIM(category_name)));

CREATE TABLE subcategories
(
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    category_id             BIGINT       NOT NULL,
    subcategory_name        VARCHAR(100) NOT NULL
        CHECK (TRIM(subcategory_name) <> ''),
    subcategory_description TEXT,
    is_active               BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by              VARCHAR(100) NOT NULL
        CHECK (TRIM(created_by) <> ''),
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by              VARCHAR(100),
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
    category_type        BIGINT       NOT NULL,
    category_name        VARCHAR(100) NOT NULL
        CHECK (TRIM(category_name) <> ''),
    category_description TEXT,
    is_active            BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by           VARCHAR(100) NOT NULL
        CHECK (TRIM(created_by) <> ''),
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by           VARCHAR(100),
    updated_at           TIMESTAMPTZ,

    CONSTRAINT fk_user_categories_user
        FOREIGN KEY (user_id)
            REFERENCES users (id),
    CONSTRAINT fk_user_category_type
        FOREIGN KEY (category_type)
            REFERENCES reference_object (id)
);

CREATE UNIQUE INDEX ux_user_category
    ON user_categories (user_id, category_type, LOWER(TRIM(category_name)));

CREATE TABLE user_subcategories
(
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_category_id        BIGINT       NOT NULL,
    subcategory_name        VARCHAR(100) NOT NULL
        CHECK (TRIM(subcategory_name) <> ''),
    subcategory_description TEXT,
    is_active               BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by              VARCHAR(100) NOT NULL
        CHECK (TRIM(created_by) <> ''),
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by              VARCHAR(100),
    updated_at              TIMESTAMPTZ,

    CONSTRAINT fk_user_subcategories_user_category
        FOREIGN KEY (user_category_id)
            REFERENCES user_categories (id)
);

CREATE UNIQUE INDEX ux_user_category_subcategory
    ON user_subcategories (user_category_id, LOWER(TRIM(subcategory_name)));