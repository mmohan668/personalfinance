-- ============================================================
-- Create Users Password History Trigger
-- ============================================================

DROP TRIGGER IF EXISTS trg_users_password_history ON users;

CREATE TRIGGER trg_users_password_history
    AFTER INSERT OR
UPDATE OF password_hash
ON users
    FOR EACH ROW
    EXECUTE FUNCTION fn_users_password_history();