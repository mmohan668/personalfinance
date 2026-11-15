DROP TRIGGER IF EXISTS trg_user_auth_providers_history
    ON user_auth_providers;

CREATE TRIGGER trg_user_auth_providers_history
    AFTER INSERT OR UPDATE OR DELETE
    ON user_auth_providers
    FOR EACH ROW
EXECUTE FUNCTION fn_user_auth_providers_history();