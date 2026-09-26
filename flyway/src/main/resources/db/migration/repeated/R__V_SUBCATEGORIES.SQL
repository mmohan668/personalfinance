DROP VIEW IF EXISTS v_subcategories_view;

CREATE VIEW v_subcategories_view AS
SELECT usc.id,
       uc.id             AS user_category_id,
       ro.id             AS transaction_type_id,
       ro.reference_code AS transaction_type,
       uc.category_name,
       uc.category_description,
       u.admin_user_id,
       usc.subcategory_name,
       usc.subcategory_description,
       usc.is_active,
       cu.username       AS created_by,
       usc.created_at,
       uu.username       AS updated_by,
       usc.updated_at
FROM user_categories uc
         LEFT JOIN user_subcategories usc
                   ON uc.id = usc.user_category_id
         LEFT JOIN users u
                   ON u.admin_user_id = uc.user_id
         LEFT JOIN users cu
                   ON cu.id = usc.created_by
         LEFT JOIN users uu
                   ON uu.id = usc.updated_by
         LEFT JOIN reference_value ro
                   ON ro.id = uc.transaction_type;