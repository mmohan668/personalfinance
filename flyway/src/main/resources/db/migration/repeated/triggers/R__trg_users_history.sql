DROP TRIGGER IF EXISTS trg_users_history ON users;

CREATE TRIGGER trg_users_history
    AFTER INSERT OR UPDATE
    ON users
    FOR EACH ROW
EXECUTE FUNCTION fn_users_history();