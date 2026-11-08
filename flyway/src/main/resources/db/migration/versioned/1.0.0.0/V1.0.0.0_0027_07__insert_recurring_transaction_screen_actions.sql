DO
$$
    DECLARE
        v_system_user_id BIGINT;
        v_screen_id BIGINT;
    BEGIN
        SELECT u.id
        INTO v_system_user_id
        FROM users u
        WHERE LOWER(TRIM(u.username)) = LOWER(TRIM('System'))
          AND u.is_deleted = FALSE;

        IF v_system_user_id IS NULL THEN
            RAISE EXCEPTION 'Active System user does not exist. Cannot seed recurring transaction screen.';
        END IF;

        INSERT INTO app_screen
            (module_id, screen_name, route_path, description, display_order, created_by)
        SELECT m.id, 'Recurring Transactions', '/recurringTransactions',
               'Admin-only management of automated recurring financial transactions.', 4, v_system_user_id
        FROM app_module m
        WHERE LOWER(TRIM(m.module_name)) = LOWER(TRIM('Settings'))
          AND NOT EXISTS (
              SELECT 1
              FROM app_screen s
              WHERE s.module_id = m.id
                AND LOWER(TRIM(s.screen_name)) = LOWER(TRIM('Recurring Transactions'))
          );

        SELECT s.id
        INTO v_screen_id
        FROM app_screen s
                 JOIN app_module m ON m.id = s.module_id
        WHERE LOWER(TRIM(m.module_name)) = LOWER(TRIM('Settings'))
          AND LOWER(TRIM(s.screen_name)) = LOWER(TRIM('Recurring Transactions'));

        IF v_screen_id IS NULL THEN
            RAISE EXCEPTION 'Settings module or Recurring Transactions screen is missing.';
        END IF;

        INSERT INTO app_action (screen_id, action_name, description, display_order, created_by)
        SELECT v_screen_id, a.action_name, a.description, a.display_order, v_system_user_id
        FROM (VALUES
            ('Add', 'Create recurring transaction rules.', 1),
            ('Edit', 'Update recurring transaction rules.', 2),
            ('Delete', 'Delete recurring transaction rules.', 3),
            ('Activate', 'Activate recurring transaction rules.', 4),
            ('Inactivate', 'Inactivate recurring transaction rules.', 5)
        ) AS a(action_name, description, display_order)
        WHERE NOT EXISTS (
            SELECT 1
            FROM app_action existing
            WHERE existing.screen_id = v_screen_id
              AND LOWER(TRIM(existing.action_name)) = LOWER(TRIM(a.action_name))
        );
    END;
$$;
