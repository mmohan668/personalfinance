-- ============================================================
-- Create Users History Trigger Function
-- ============================================================

CREATE
OR REPLACE FUNCTION fn_users_history()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
DECLARE
v_action_by VARCHAR(100);
BEGIN

    -- ========================================================
    -- 1. SKIP UNCHANGED UPDATE OPERATIONS
    -- ========================================================

    IF
TG_OP = 'UPDATE' THEN
        IF OLD IS NOT DISTINCT FROM NEW THEN
            RETURN NEW;
END IF;
END IF;


    -- ========================================================
    -- 2. DETERMINE ACTION USER
    -- ========================================================

    IF
TG_OP = 'DELETE' THEN

        v_action_by := COALESCE(
                OLD.updated_by,
                OLD.created_by,
                'SYSTEM'
                       );

ELSE

        v_action_by := COALESCE(
                NEW.updated_by,
                NEW.created_by,
                'SYSTEM'
                       );

END IF;


    -- ========================================================
    -- 3. INSERT HISTORY RECORD
    -- ========================================================

    IF
TG_OP = 'DELETE' THEN

        INSERT INTO users_history
        (user_id,
         admin_user_id,
         username,
         display_name,
         email,
         must_change_password,
         is_active,
         created_by,
         created_at,
         updated_by,
         updated_at,
         is_deleted,
         deleted_by,
         deleted_at,
         action_type,
         action_by,
         action_at)
        VALUES (OLD.id,
                OLD.admin_user_id,
                OLD.username,
                OLD.display_name,
                OLD.email,
                OLD.must_change_password,
                OLD.is_active,
                OLD.created_by,
                OLD.created_at,
                OLD.updated_by,
                OLD.updated_at,
                OLD.is_deleted,
                OLD.deleted_by,
                OLD.deleted_at,
                TG_OP,
                v_action_by,
                clock_timestamp());

RETURN OLD;

ELSE

        INSERT INTO users_history
        (user_id,
         admin_user_id,
         username,
         display_name,
         email,
         must_change_password,
         is_active,
         created_by,
         created_at,
         updated_by,
         updated_at,
         is_deleted,
         deleted_by,
         deleted_at,
         action_type,
         action_by,
         action_at)
        VALUES (NEW.id,
                NEW.admin_user_id,
                NEW.username,
                NEW.display_name,
                NEW.email,
                NEW.must_change_password,
                NEW.is_active,
                NEW.created_by,
                NEW.created_at,
                NEW.updated_by,
                NEW.updated_at,
                NEW.is_deleted,
                NEW.deleted_by,
                NEW.deleted_at,
                TG_OP,
                v_action_by,
                clock_timestamp());

RETURN NEW;

END IF;

END;
$$;