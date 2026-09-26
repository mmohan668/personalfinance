DROP VIEW IF EXISTS v_financial_transactions;

CREATE VIEW v_financial_transactions AS
SELECT ft.id,
       ft.transaction_at,
       ft.amount,
       transaction_type.id                 AS transaction_type_id,
       transaction_type.reference_code     AS transaction_type,
       uc.id                               AS category_id,
       uc.category_name,
       uc.category_description,
       usc.id                              AS subcategory_id,
       usc.subcategory_name,
       usc.subcategory_description,
       ft.remarks,
       location.id                         AS location_id,
       location.reference_code             AS location,
       location.reference_code_description AS location_description,
       ft.admin_user_id,
       u1.username                         AS created_by,
       ft.created_at,
       u2.username                         AS updated_by,
       ft.updated_at
FROM financial_transactions ft
         LEFT JOIN reference_value transaction_type
                   ON transaction_type.id = ft.transaction_type_id
         LEFT JOIN reference_value location
                   ON location.id = ft.location_id
         LEFT JOIN user_categories uc
                   ON uc.id = ft.category_id
         LEFT JOIN user_subcategories usc
                   ON usc.id = ft.subcategory_id AND usc.user_category_id = uc.id
         LEFT JOIN users u1
                   ON u1.id = ft.created_by
         LEFT JOIN users u2
                   ON u2.id = ft.updated_by;

