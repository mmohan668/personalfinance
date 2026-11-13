CREATE
    OR REPLACE FUNCTION fn_users_password_history()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_action_type VARCHAR(10);
    v_changed_by
                  VARCHAR(100);
BEGIN

    IF
        TG_OP = 'INSERT' THEN

        v_action_type := 'INITIAL';

        v_changed_by
            := COALESCE(
                NEW.created_by,
                'SYSTEM');

    ELSIF
        TG_OP = 'UPDATE' THEN

        IF OLD.password_hash IS NOT DISTINCT FROM NEW.password_hash THEN
            RETURN NEW;
        END IF;

        v_action_type
            := 'CHANGE';

        v_changed_by
            := COALESCE(
                NEW.updated_by,
                NEW.created_by,
                'SYSTEM');

    END IF;

    INSERT INTO users_password_history
    (user_id,
     password_hash,
     action_type,
     changed_by,
     changed_at)
    VALUES (NEW.id,
            NEW.password_hash,
            v_action_type,
            v_changed_by,
            clock_timestamp());

    RETURN NEW;

END;
$$;