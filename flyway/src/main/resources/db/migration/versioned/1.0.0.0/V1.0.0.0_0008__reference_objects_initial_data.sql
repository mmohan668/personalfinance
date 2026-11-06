-- =========================================================
-- Validate system user
-- =========================================================

DO
$$
    DECLARE
v_system_user_count INTEGER;
BEGIN
SELECT COUNT(*)
INTO v_system_user_count
FROM users
WHERE LOWER(TRIM(username)) = 'system'
  AND is_deleted = FALSE;

IF
v_system_user_count <> 1 THEN
            RAISE EXCEPTION
                'Expected exactly one active System user, but found %.',
                v_system_user_count;
END IF;
END;
$$;

-- =========================================================
-- Insert initial reference objects
-- =========================================================

WITH system_account AS
         (SELECT id
          FROM users
          WHERE LOWER(TRIM(username)) = 'system'
            AND is_deleted = FALSE)
INSERT
INTO reference_object
(ref_obj_name,
 created_by)
SELECT ref_objects.ref_obj_name,
       system_account.id
FROM system_account
         CROSS JOIN
     (VALUES ('TRANSACTION_TYPE'),
             ('CURRENCY_CODE'),
             ('DATE_FORMAT'),
             ('LOCATION')) AS ref_objects (ref_obj_name)
         ON CONFLICT DO NOTHING;