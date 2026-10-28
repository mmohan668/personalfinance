-- =========================================================
-- 1. Create users table
-- =========================================================

CREATE TABLE users
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    admin_user_id BIGINT,
    username      VARCHAR(100) NOT NULL CHECK (TRIM(username) <> ''),
    display_name  VARCHAR(100) NOT NULL CHECK (TRIM(display_name) <> ''),
    email         VARCHAR(255) NOT NULL CHECK (TRIM(email) <> ''),
    password_hash VARCHAR(255) NOT NULL CHECK (TRIM(password_hash) <> ''),
    is_active     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_by    VARCHAR(100) NOT NULL CHECK (TRIM(created_by) <> ''),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by    VARCHAR(100),
    updated_at    TIMESTAMPTZ,

    CONSTRAINT fk_users_admin_user
        FOREIGN KEY (admin_user_id)
            REFERENCES users (id)
);

-- =========================================================
-- 2. Create users indexes
-- =========================================================

CREATE UNIQUE INDEX ux_users_user_name
    ON users (LOWER(TRIM(username)));

CREATE UNIQUE INDEX ux_users_email
    ON users (LOWER(TRIM(email)));

CREATE INDEX idx_users_admin_user_id
    ON users (admin_user_id);

-- =========================================================
-- 3. Insert system user
-- =========================================================

INSERT INTO users
(username,
 display_name,
 email,
 password_hash,
 created_by)
VALUES ('System',
        'PERSONAL FINANCE APP',
        'personalfinanceapp.support@gmail.com',
        'PASSWORD_HASH_TEMP',
        'FLYWAY');

-- Associate system user with itself
UPDATE users
SET admin_user_id = id,
    is_active     = TRUE
WHERE username = 'System';

-- =========================================================
-- 4. Create users history table
-- =========================================================

CREATE TABLE users_history
(
    history_id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id       BIGINT       NOT NULL,
    admin_user_id BIGINT,
    username      VARCHAR(100) NOT NULL,
    display_name  VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    is_active     BOOLEAN      NOT NULL,
    created_by    VARCHAR(100) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL,
    updated_by    VARCHAR(100),
    updated_at    TIMESTAMPTZ,

    action_type   VARCHAR(10)  NOT NULL
        CHECK (action_type IN ('INSERT', 'UPDATE', 'DELETE')),

    action_by     VARCHAR(100) NOT NULL,
    action_at     TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================================================
-- 5. Create history indexes
-- =========================================================

CREATE INDEX idx_users_history_user_id
    ON users_history (user_id);

CREATE INDEX idx_users_history_action_at
    ON users_history (action_at DESC);

-- =========================================================
-- 6. Create users history trigger function
-- =========================================================

CREATE OR REPLACE FUNCTION fn_users_history()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_action_by VARCHAR(100);
BEGIN
    -- Skip history when an UPDATE does not change any values
    IF TG_OP = 'UPDATE' THEN
        IF OLD IS NOT DISTINCT FROM NEW THEN
            RETURN NEW;
        END IF;
    END IF;

    -- Handle DELETE operations
    IF TG_OP = 'DELETE' THEN

        v_action_by := COALESCE(
                OLD.updated_by,
                OLD.created_by,
                'SYSTEM'
                       );

        INSERT INTO users_history
        (user_id,
         admin_user_id,
         username,
         display_name,
         email,
         is_active,
         created_by,
         created_at,
         updated_by,
         updated_at,
         action_type,
         action_by,
         action_at)
        VALUES (OLD.id,
                OLD.admin_user_id,
                OLD.username,
                OLD.display_name,
                OLD.email,
                OLD.is_active,
                OLD.created_by,
                OLD.created_at,
                OLD.updated_by,
                OLD.updated_at,
                TG_OP,
                v_action_by,
                CURRENT_TIMESTAMP);

        RETURN OLD;

    ELSE

        -- Handle INSERT and UPDATE operations
        v_action_by := COALESCE(
                NEW.updated_by,
                NEW.created_by,
                'SYSTEM'
                       );

        INSERT INTO users_history
        (user_id,
         admin_user_id,
         username,
         display_name,
         email,
         is_active,
         created_by,
         created_at,
         updated_by,
         updated_at,
         action_type,
         action_by,
         action_at)
        VALUES (NEW.id,
                NEW.admin_user_id,
                NEW.username,
                NEW.display_name,
                NEW.email,
                NEW.is_active,
                NEW.created_by,
                NEW.created_at,
                NEW.updated_by,
                NEW.updated_at,
                TG_OP,
                v_action_by,
                CURRENT_TIMESTAMP);

        RETURN NEW;

    END IF;
END;
$$;

-- =========================================================
-- 7. Create users history trigger
-- =========================================================

CREATE TRIGGER trg_users_history
    AFTER INSERT OR UPDATE OR DELETE
    ON users
    FOR EACH ROW
EXECUTE FUNCTION fn_users_history();