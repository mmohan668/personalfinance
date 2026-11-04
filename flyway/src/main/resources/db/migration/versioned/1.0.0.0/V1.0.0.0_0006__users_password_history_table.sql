-- =========================================================
-- Create users password history table
-- =========================================================

CREATE TABLE users_password_history
(
    password_history_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id             BIGINT       NOT NULL,

    password_hash       VARCHAR(255) NOT NULL
        CHECK (TRIM(password_hash) <> ''),

    action_type         VARCHAR(10)  NOT NULL DEFAULT 'CHANGE'
        CHECK (action_type IN ('INITIAL', 'CHANGE')),

    changed_by          VARCHAR(100) NOT NULL
        CHECK (TRIM(changed_by) <> ''),

    changed_at          TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_users_password_history_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
);

-- =========================================================
-- Create indexes
-- =========================================================

CREATE INDEX idx_users_password_history_user_changed_at
    ON users_password_history (user_id, changed_at DESC);

CREATE INDEX idx_users_password_history_changed_at
    ON users_password_history (changed_at DESC);

-- The System account is created before repeatable triggers are installed.
-- Record its initial password hash explicitly; subsequent user inserts and
-- password changes are captured by fn_users_password_history().
INSERT INTO users_password_history
(
    user_id,
    password_hash,
    action_type,
    changed_by,
    changed_at
)
SELECT
    id,
    password_hash,
    'INITIAL',
    created_by,
    created_at
FROM users
WHERE LOWER(TRIM(username)) = 'system'
  AND is_deleted = FALSE
ON CONFLICT DO NOTHING;
