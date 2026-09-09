CREATE TABLE grid_name
(
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL CHECK ( TRIM(name) <> '' )
);

CREATE UNIQUE INDEX ux_grid_name
    ON grid_name (LOWER(TRIM(name)));

CREATE TABLE grid_column
(
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    grid_name_id            BIGINT       NOT NULL REFERENCES grid_name (id) ON DELETE CASCADE,
    field                   VARCHAR(50)  NOT NULL
        CHECK ( TRIM(field) <> '' ),
    header                  VARCHAR(100) NOT NULL
        CHECK ( TRIM(header) <> '' ),
    data_type               VARCHAR(50)  NOT NULL
        CHECK ( TRIM(data_type) <> '' ),
    sortable                BOOLEAN      NOT NULL DEFAULT TRUE,
    default_sort_order      VARCHAR(5)
        CHECK ( default_sort_order IS NULL OR default_sort_order IN ('asc', 'desc')),
    sort_index              INTEGER
        CHECK ( sort_index >= 0 ),
    filterable              BOOLEAN      NOT NULL DEFAULT TRUE,
    default_filter_operator VARCHAR(30)
        CHECK ( default_filter_operator IS NULL OR TRIM(default_filter_operator) <> '' ),
    width                   INTEGER
        CHECK (width IS NULL OR width >= 0 ),
    align                   VARCHAR(10)  NOT NULL DEFAULT 'LEFT'
        CHECK ( align IN ('LEFT', 'RIGHT', 'CENTER')),
    visible                 BOOLEAN      NOT NULL DEFAULT TRUE,
    visible_index           INTEGER      NOT NULL
        CHECK ( visible_index >= 0 ),
    cell_template           VARCHAR(100)
        CHECK ( cell_template IS NULL OR TRIM(cell_template) <> '' )
);

CREATE UNIQUE INDEX ux_grid_column_grid_field
    ON grid_column (grid_name_id, LOWER(TRIM(field)));

CREATE UNIQUE INDEX ux_grid_column_visible_index
    ON grid_column (grid_name_id, visible_index);

CREATE TABLE grid_personalization
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    grid_name_id BIGINT      NOT NULL REFERENCES grid_name (id) ON DELETE CASCADE,
    user_id      BIGINT      NOT NULL,
    grid_column  JSONB       NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMPTZ
);

CREATE UNIQUE INDEX uq_grid_user
    ON grid_personalization (grid_name_id, user_id);