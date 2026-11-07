-- =========================================================
-- Create user permissions view
-- =========================================================

DROP VIEW IF EXISTS vw_user_permissions;

CREATE VIEW vw_user_permissions AS
SELECT up.*,
       m.module_name,
       m.display_order AS module_display_order,
       s.screen_name,
       s.display_order AS screen_display_order,
       a.action_name,
       a.display_order AS action_display_order
FROM user_permissions up
         JOIN app_module m
              ON m.id = up.module_id
         JOIN app_screen s
              ON s.id = up.screen_id
         JOIN app_action a
              ON a.id = up.action_id;
