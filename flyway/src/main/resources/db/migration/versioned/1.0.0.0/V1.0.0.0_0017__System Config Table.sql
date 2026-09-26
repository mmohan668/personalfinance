CREATE TABLE system_config
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    config_name  VARCHAR(100)  NOT NULL CHECK ( TRIM(config_name) <> '' ),
    config_value BIGINT        NOT NULL REFERENCES reference_value (id),
    description  VARCHAR(1000) NOT NULL CHECK ( TRIM(description) <> '' ),
    created_by   BIGINT        NOT NULL REFERENCES users (id),
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by   BIGINT REFERENCES users (id),
    updated_at   TIMESTAMPTZ
);