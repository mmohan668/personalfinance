-- =========================================================
-- Insert system user
-- =========================================================

WITH system_account AS
         (
             INSERT INTO users
                 (
                  username,
                  display_name,
                  email,
                  password_hash,
                  is_active,
                  created_by
                     )
                 VALUES ('System',
                         'System',
                         'personalfinanceapp.support@gmail.com',
                         'PASSWORD_HASH_TEMP',
                         TRUE,
                         'FLYWAY')
                 RETURNING id)

-- =========================================================
-- Associate system user with itself
-- =========================================================

UPDATE users
SET admin_user_id = system_account.id
FROM system_account
WHERE users.id = system_account.id;