DO
$$
    DECLARE
        v_system_user_id BIGINT;
        v_screen_id      BIGINT;
    BEGIN
        SELECT u.id
        INTO v_system_user_id
        FROM users u
        WHERE LOWER(TRIM(u.username)) = LOWER(TRIM('System'))
          AND u.is_deleted = FALSE;

        IF v_system_user_id IS NULL THEN
            RAISE EXCEPTION 'Active System user does not exist. Cannot seed User Management actions.';
        END IF;

        SELECT s.id
        INTO v_screen_id
        FROM app_screen s
                 JOIN app_module m ON m.id = s.module_id
        WHERE LOWER(TRIM(m.module_name)) = LOWER(TRIM('User Management'))
          AND LOWER(TRIM(s.screen_name)) = LOWER(TRIM('Users'));

        IF v_screen_id IS NULL THEN
            RAISE EXCEPTION 'User Management / Users screen does not exist. Cannot seed its actions.';
        END IF;

        INSERT INTO app_action
            (screen_id, action_name, description, display_order, created_by)
        SELECT v_screen_id, a.action_name, a.description, a.display_order, v_system_user_id
        FROM (VALUES ('Edit', 'Update the selected user.', 1)) AS a(action_name, description, display_order)
        WHERE NOT EXISTS (SELECT 1
                          FROM app_action existing
                          WHERE existing.screen_id = v_screen_id
                            AND LOWER(TRIM(existing.action_name)) = LOWER(TRIM(a.action_name)));
    END;
$$;
