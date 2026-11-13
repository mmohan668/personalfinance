CREATE OR REPLACE PROCEDURE initialize_user_permissions(
    IN p_user_id BIGINT,
    IN p_created_by BIGINT
)
    LANGUAGE plpgsql
AS
$$
BEGIN
    IF p_user_id IS NULL THEN
        RAISE EXCEPTION 'User ID is required';
    END IF;

    IF NOT EXISTS (SELECT 1
                   FROM users
                   WHERE id = p_user_id
                     AND is_deleted = FALSE) THEN
        RAISE EXCEPTION 'User with id % does not exist or is deleted', p_user_id;
    END IF;

    IF p_created_by IS NULL OR NOT EXISTS (SELECT 1
                                           FROM users
                                           WHERE id = p_created_by) THEN
        RAISE EXCEPTION 'Valid created-by user ID is required';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM app_action) THEN
        RAISE EXCEPTION 'No application actions exist. Cannot initialize user permissions.';
    END IF;

    INSERT INTO user_permissions
        (user_id, module_id, screen_id, action_id, has_permission, created_by)
    SELECT p_user_id,
           s.module_id,
           s.id,
           a.id,
           FALSE,
           p_created_by
    FROM app_action a
             JOIN app_screen s
                  ON s.id = a.screen_id
    ON CONFLICT (user_id, action_id) DO NOTHING;
END;
$$;
