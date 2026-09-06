CREATE TABLE categories
(
    id                   BIGSERIAL PRIMARY KEY,
    transaction_type     VARCHAR(20)  NOT NULL
        CHECK (transaction_type IN ('EXPENSE', 'INCOME', 'TRANSFER', 'INVESTMENT')),
    category_name        VARCHAR(100) NOT NULL CHECK ( TRIM(category_name) <> '' ),
    category_description VARCHAR(100),
    is_active            BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by           VARCHAR(100) NOT NULL CHECK ( TRIM(created_by) <> '' ),
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by           VARCHAR(100),
    updated_at           TIMESTAMPTZ,

    CONSTRAINT uq_transaction_category
        UNIQUE (transaction_type, category_name)
);

CREATE TABLE subcategories
(
    id                      BIGSERIAL PRIMARY KEY,
    category_id             BIGINT       NOT NULL,
    subcategory_name        VARCHAR(100) NOT NULL CHECK (TRIM(subcategory_name) <> ''),
    subcategory_description VARCHAR(100),
    is_active               BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by              VARCHAR(100) NOT NULL CHECK (TRIM(created_by) <> ''),
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by              VARCHAR(100),
    updated_at              TIMESTAMPTZ,

    CONSTRAINT fk_subcategories_category
        FOREIGN KEY (category_id)
            REFERENCES categories (id),

    CONSTRAINT uq_category_subcategory
        UNIQUE (category_id, subcategory_name)
);

CREATE TABLE user_categories
(
    id                   BIGSERIAL PRIMARY KEY,
    user_id              BIGINT       NOT NULL,
    transaction_type     VARCHAR(20)  NOT NULL
        CHECK (transaction_type IN ('EXPENSE', 'INCOME', 'TRANSFER', 'INVESTMENT')),
    category_name        VARCHAR(100) NOT NULL CHECK (TRIM(category_name) <> ''),
    category_description VARCHAR(100),
    is_active            BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by           VARCHAR(100) NOT NULL CHECK (TRIM(created_by) <> ''),
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by           VARCHAR(100),
    updated_at           TIMESTAMPTZ,

    CONSTRAINT uq_user_transaction_category
        UNIQUE (user_id, transaction_type, category_name),

    CONSTRAINT fk_user_categories_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
);

CREATE TABLE user_subcategories
(
    id                      BIGSERIAL PRIMARY KEY,
    user_category_id        BIGINT       NOT NULL,
    subcategory_name        VARCHAR(100) NOT NULL CHECK (TRIM(subcategory_name) <> ''),
    subcategory_description VARCHAR(100),
    is_active               BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by              VARCHAR(100) NOT NULL CHECK (TRIM(created_by) <> ''),
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by              VARCHAR(100),
    updated_at              TIMESTAMPTZ,

    CONSTRAINT fk_user_subcategories_user_category
        FOREIGN KEY (user_category_id)
            REFERENCES user_categories (id),

    CONSTRAINT uq_user_category_subcategory
        UNIQUE (user_category_id, subcategory_name)
);