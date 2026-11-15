CREATE OR REPLACE FUNCTION fn_user_auth_providers_history()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_action_by VARCHAR(100);
BEGIN

    IF TG_OP = 'UPDATE' THEN
        IF OLD IS NOT DISTINCT FROM NEW THEN
            RETURN NEW;
        END IF;
    END IF;

    IF TG_OP = 'DELETE' THEN

        v_action_by := COALESCE(
                OLD.created_by,
                'SYSTEM'
                       );

        INSERT INTO user_auth_providers_history
        (provider_id,
         user_id,
         provider,
         provider_user_id,
         provider_email,
         created_by,
         created_at,
         action_type,
         action_by,
         action_at)
        VALUES (OLD.id,
                OLD.user_id,
                OLD.provider,
                OLD.provider_user_id,
                OLD.provider_email,
                OLD.created_by,
                OLD.created_at,
                TG_OP,
                v_action_by,
                clock_timestamp());

        RETURN OLD;

    ELSE

        v_action_by := COALESCE(
                NEW.created_by,
                'SYSTEM'
                       );

        INSERT INTO user_auth_providers_history
        (provider_id,
         user_id,
         provider,
         provider_user_id,
         provider_email,
         created_by,
         created_at,
         action_type,
         action_by,
         action_at)
        VALUES (NEW.id,
                NEW.user_id,
                NEW.provider,
                NEW.provider_user_id,
                NEW.provider_email,
                NEW.created_by,
                NEW.created_at,
                TG_OP,
                v_action_by,
                clock_timestamp());

        RETURN NEW;

    END IF;

END;
$$;