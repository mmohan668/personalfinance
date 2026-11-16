WITH system_account AS
         (
             INSERT
                 INTO users
                     (username,
                      display_name,
                      email,
                      password_hash,
                      is_active,
                      auth_provider,
                      created_by)
                     VALUES ('System',
                             'System',
                             'personalfinanceapp.support@gmail.com',
                             'PASSWORD_HASH_TEMP',
                             TRUE,
                             'default',
                             'FLYWAY')
                     RETURNING id)

UPDATE users
SET admin_user_id = system_account.id
FROM system_account
WHERE users.id = system_account.id;