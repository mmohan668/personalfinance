CREATE TABLE user_auth_providers_history
(
    history_id       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    provider_id      BIGINT       NOT NULL,
    user_id          BIGINT       NOT NULL,
    provider         VARCHAR(30)  NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    provider_email   VARCHAR(255),
    created_by       VARCHAR(100) NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL,
    action_type      VARCHAR(10)  NOT NULL
        CHECK (action_type IN ('INSERT', 'UPDATE', 'DELETE')),
    action_by        VARCHAR(100) NOT NULL,
    action_at        TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_user_auth_providers_history_provider
        CHECK (provider IN ('GOOGLE'))
);

CREATE INDEX idx_user_auth_providers_history_provider_id_action_at
    ON user_auth_providers_history (provider_id, action_at DESC);

CREATE INDEX idx_user_auth_providers_history_user_id_action_at
    ON user_auth_providers_history (user_id, action_at DESC);

CREATE INDEX idx_user_auth_providers_history_action_at
    ON user_auth_providers_history (action_at DESC);
