CREATE TABLE user_auth_providers
(
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id          BIGINT       NOT NULL,
    provider         VARCHAR(30)  NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    provider_email   VARCHAR(255),
    created_by       VARCHAR(100) NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_auth_providers_user
        FOREIGN KEY (user_id)
            REFERENCES users (id),

    CONSTRAINT chk_user_auth_providers_provider
        CHECK (provider IN ('GOOGLE')),

    CONSTRAINT uq_user_auth_providers_identity
        UNIQUE (provider, provider_user_id),

    CONSTRAINT uq_user_auth_providers_user_provider
        UNIQUE (user_id, provider)
);

CREATE INDEX idx_user_auth_providers_user_id
    ON user_auth_providers (user_id);

CREATE INDEX idx_user_auth_providers_provider_email
    ON user_auth_providers (LOWER(TRIM(provider_email)));