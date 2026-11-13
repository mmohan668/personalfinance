DO
$$
    DECLARE
        v_system_user_id BIGINT;
        v_screen_count   BIGINT;
    BEGIN
        SELECT u.id
        INTO v_system_user_id
        FROM users u
        WHERE LOWER(TRIM(u.username)) = LOWER(TRIM('System'))
          AND u.is_deleted = FALSE;

        IF v_system_user_id IS NULL THEN
            RAISE EXCEPTION 'Active System user does not exist. Cannot seed financial transaction screen actions.';
        END IF;

        SELECT COUNT(DISTINCT LOWER(TRIM(s.screen_name)))
        INTO v_screen_count
        FROM app_screen s
                 JOIN app_module m ON m.id = s.module_id
        WHERE LOWER(TRIM(m.module_name)) = LOWER(TRIM('Financial Transaction'))
          AND LOWER(TRIM(s.screen_name)) IN ('all transactions', 'expense', 'income', 'investment', 'transfer');

        IF v_screen_count <> 5 THEN
            RAISE EXCEPTION 'Expected all five Financial Transaction screens before seeding actions; found %.', v_screen_count;
        END IF;

        INSERT INTO app_action
            (screen_id, action_name, description, display_order, created_by)
        SELECT s.id, a.action_name, a.description, a.display_order, v_system_user_id
        FROM app_screen s
                 JOIN app_module m ON m.id = s.module_id
                 CROSS JOIN (VALUES ('Add', 'Create a financial transaction.', 1),
                                    ('Copy', 'Create a transaction by copying the selected transaction.', 2),
                                    ('Edit', 'Update the selected transaction.', 3),
                                    ('Delete', 'Delete the selected transaction.',
                                     4)) AS a(action_name, description, display_order)
        WHERE NOT EXISTS (SELECT 1
                          FROM app_action existing
                          WHERE existing.screen_id = s.id
                            AND LOWER(TRIM(existing.action_name)) = LOWER(TRIM(a.action_name)))
          AND LOWER(TRIM(m.module_name)) = LOWER(TRIM('Financial Transaction'))
          AND LOWER(TRIM(s.screen_name)) IN ('all transactions', 'expense', 'income', 'investment', 'transfer');
    END;
$$;
