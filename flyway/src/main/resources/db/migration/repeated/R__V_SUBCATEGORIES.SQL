DROP VIEW IF EXISTS v_subcategories_view;

CREATE OR REPLACE VIEW v_subcategories_view AS
SELECT usc.id,
       uc.id             AS user_category_id,
       ro.id             AS category_type_id,
       ro.reference_code AS category_type,
       uc.category_name,
       uc.category_description,
       u.admin_user_id,
       usc.subcategory_name,
       usc.subcategory_description,
       usc.is_active,
       usc.created_by,
       usc.created_at,
       usc.updated_by,
       usc.updated_at
FROM user_categories uc
         JOIN user_subcategories usc
              ON uc.id = usc.user_category_id
         JOIN users u
              ON u.admin_user_id = uc.user_id
         JOIN reference_value ro
              ON ro.id = uc.category_type;