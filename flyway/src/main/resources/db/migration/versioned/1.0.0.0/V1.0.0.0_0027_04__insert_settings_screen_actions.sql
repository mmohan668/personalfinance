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
            RAISE EXCEPTION 'Active System user does not exist. Cannot seed Settings actions.';
        END IF;

        SELECT COUNT(DISTINCT LOWER(TRIM(s.screen_name)))
        INTO v_screen_count
        FROM app_screen s
                 JOIN app_module m ON m.id = s.module_id
        WHERE LOWER(TRIM(m.module_name)) = LOWER(TRIM('Settings'))
          AND LOWER(TRIM(s.screen_name)) IN ('system config', 'reference values');

        IF v_screen_count <> 2 THEN
            RAISE EXCEPTION 'Expected the System Config and Reference Values screens before seeding actions; found %.', v_screen_count;
        END IF;

        INSERT INTO app_action
            (screen_id, action_name, description, display_order, created_by)
        SELECT s.id, a.action_name, a.description, a.display_order, v_system_user_id
        FROM app_screen s
                 JOIN app_module m ON m.id = s.module_id
                 JOIN (VALUES ('System Config', 'Edit', 'Update the selected system configuration.', 1),
                              ('Reference Values', 'Add', 'Create a reference value.', 1),
                              ('Reference Values', 'Copy', 'Create a reference value by copying the selected value.',
                               2),
                              ('Reference Values', 'Edit', 'Update the selected reference value.', 3),
                              ('Reference Values', 'Delete', 'Delete the selected reference value.', 4),
                              ('Reference Values', 'Activate', 'Activate the selected reference value.', 5),
                              ('Reference Values', 'Inactivate', 'Inactivate the selected reference value.',
                               6)) AS a(screen_name, action_name, description, display_order)
                      ON LOWER(TRIM(s.screen_name)) = LOWER(TRIM(a.screen_name))
        WHERE LOWER(TRIM(m.module_name)) = LOWER(TRIM('Settings'))
          AND NOT EXISTS (SELECT 1
                          FROM app_action existing
                          WHERE existing.screen_id = s.id
                            AND LOWER(TRIM(existing.action_name)) = LOWER(TRIM(a.action_name)));
    END;
$$;
