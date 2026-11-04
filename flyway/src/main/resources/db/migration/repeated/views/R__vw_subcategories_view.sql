-- ============================================================
-- Create Subcategories View
-- ============================================================

DROP VIEW IF EXISTS vw_subcategories_view;

CREATE VIEW vw_subcategories_view AS
SELECT usc.id,
       uc.user_id,
       uc.id              AS user_category_id,

       urv.id             AS transaction_type_id,
       urv.reference_code AS transaction_type,

       uc.category_name,
       uc.category_description,

       u.admin_user_id,

       usc.subcategory_name,
       usc.subcategory_description,
       usc.is_active,

       cu.username        AS created_by,
       usc.created_at,

       uu.username        AS updated_by,
       usc.updated_at

FROM user_categories uc

         LEFT JOIN user_subcategories usc
                   ON usc.user_category_id = uc.id

         LEFT JOIN users u
                   ON u.id = uc.user_id

         LEFT JOIN user_reference_value urv
                   ON urv.id = uc.transaction_type
                       AND urv.user_id = uc.user_id

         LEFT JOIN users cu
                   ON cu.id = usc.created_by

         LEFT JOIN users uu
                   ON uu.id = usc.updated_by;