-- =========================================================
-- Seed permission modules and their frontend screens
-- =========================================================

DO
$$
    DECLARE
        v_system_user_id BIGINT;
    BEGIN
        SELECT u.id
        INTO v_system_user_id
        FROM users u
        WHERE LOWER(TRIM(u.username)) = LOWER(TRIM('System'))
          AND u.is_deleted = FALSE;

        IF v_system_user_id IS NULL THEN
            RAISE EXCEPTION 'Active System user does not exist. Cannot seed permission modules and screens.';
        END IF;

        INSERT INTO app_module
        (module_name, description, display_order, created_by)
        SELECT m.module_name, m.description, m.display_order, v_system_user_id
        FROM (VALUES
            ('Financial Transaction', 'Manage financial transaction screens.', 1),
            ('Category Management', 'Manage categories and subcategories.', 2),
            ('Settings', 'Manage application settings and reference values.', 3),
            ('Bulk Upload', 'Upload application data from spreadsheet files.', 4),
            ('User Management', 'Manage application users.', 5)
        ) AS m(module_name, description, display_order)
        WHERE NOT EXISTS (
            SELECT 1
            FROM app_module existing
            WHERE LOWER(TRIM(existing.module_name)) = LOWER(TRIM(m.module_name))
        );

        INSERT INTO app_screen
        (module_id, screen_name, route_path, description, display_order, created_by)
        SELECT m.id, s.screen_name, s.route_path, s.description, s.display_order, v_system_user_id
        FROM (VALUES
            ('Financial Transaction', 'All Transactions', '/allTransactions', 'View all financial transactions.', 1),
            ('Financial Transaction', 'Expense', '/expenses', 'View expense transactions.', 2),
            ('Financial Transaction', 'Income', '/income', 'View income transactions.', 3),
            ('Financial Transaction', 'Investment', '/investments', 'View investment transactions.', 4),
            ('Financial Transaction', 'Transfer', '/transfers', 'View transfer transactions.', 5),
            ('Category Management', 'Category', '/categories', 'Manage transaction categories.', 1),
            ('Category Management', 'Subcategory', '/subcategories', 'Manage transaction subcategories.', 2),
            ('Settings', 'System Config', '/system-config', 'Manage system configuration values.', 1),
            ('Settings', 'Reference Values', '/reference-values', 'Manage reference values.', 2),
            ('Bulk Upload', 'Bulk Upload', '/bulkupload', 'Upload data using spreadsheet templates.', 1),
            ('User Management', 'Users', '/users', 'Manage application users.', 1)
        ) AS s(module_name, screen_name, route_path, description, display_order)
        JOIN app_module m
          ON LOWER(TRIM(m.module_name)) = LOWER(TRIM(s.module_name))
        WHERE NOT EXISTS (
            SELECT 1
            FROM app_screen existing
            WHERE existing.module_id = m.id
              AND LOWER(TRIM(existing.screen_name)) = LOWER(TRIM(s.screen_name))
        );
    END;
$$;
