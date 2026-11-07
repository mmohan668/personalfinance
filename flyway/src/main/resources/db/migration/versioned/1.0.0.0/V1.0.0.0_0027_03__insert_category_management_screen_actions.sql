-- =========================================================
-- Seed actions for Category Management screens
-- =========================================================

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
            RAISE EXCEPTION 'Active System user does not exist. Cannot seed Category Management actions.';
        END IF;

        SELECT COUNT(DISTINCT LOWER(TRIM(s.screen_name)))
        INTO v_screen_count
        FROM app_screen s
        JOIN app_module m ON m.id = s.module_id
        WHERE LOWER(TRIM(m.module_name)) = LOWER(TRIM('Category Management'))
          AND LOWER(TRIM(s.screen_name)) IN ('category', 'subcategory');

        IF v_screen_count <> 2 THEN
            RAISE EXCEPTION 'Expected the Category and Subcategory screens before seeding actions; found %.', v_screen_count;
        END IF;

        INSERT INTO app_action
        (screen_id, action_name, description, display_order, created_by)
        SELECT s.id, a.action_name, a.description, a.display_order, v_system_user_id
        FROM app_screen s
        JOIN app_module m ON m.id = s.module_id
        JOIN (VALUES
            ('Category', 'Add', 'Create a category.', 1),
            ('Category', 'Edit', 'Update the selected category.', 2),
            ('Category', 'Delete', 'Delete the selected category.', 3),
            ('Category', 'Activate', 'Activate the selected category.', 4),
            ('Category', 'Inactivate', 'Inactivate the selected category.', 5),
            ('Subcategory', 'Add', 'Create a subcategory.', 1),
            ('Subcategory', 'Copy', 'Create a subcategory by copying the selected subcategory.', 2),
            ('Subcategory', 'Edit', 'Update the selected subcategory.', 3),
            ('Subcategory', 'Delete', 'Delete the selected subcategory.', 4),
            ('Subcategory', 'Activate', 'Activate the selected subcategory.', 5),
            ('Subcategory', 'Inactivate', 'Inactivate the selected subcategory.', 6)
        ) AS a(screen_name, action_name, description, display_order)
          ON LOWER(TRIM(s.screen_name)) = LOWER(TRIM(a.screen_name))
        WHERE LOWER(TRIM(m.module_name)) = LOWER(TRIM('Category Management'))
          AND NOT EXISTS (
              SELECT 1
              FROM app_action existing
              WHERE existing.screen_id = s.id
                AND LOWER(TRIM(existing.action_name)) = LOWER(TRIM(a.action_name))
          );
    END;
$$;
