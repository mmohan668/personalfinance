-- ============================================================
-- Create Financial Transactions View
-- ============================================================

DROP VIEW IF EXISTS vw_financial_transactions;

CREATE VIEW vw_financial_transactions AS
SELECT ft.id,
       ft.transaction_at,
       ft.amount,

       -- Transaction Type
       transaction_type.id
           AS transaction_type_id,
       transaction_type.reference_code
           AS transaction_type,

       -- Category
       uc.id
           AS category_id,
       uc.category_name,
       uc.category_description,

       -- Subcategory
       usc.id
           AS subcategory_id,
       usc.subcategory_name,
       usc.subcategory_description,

       -- Transaction Details
       ft.remarks,

       -- Location
       location.id
           AS location_id,
       location.reference_code
           AS location,
       location.reference_code_description
           AS location_description,

       -- User Ownership
       uc.user_id,

       -- Admin User
       ft.admin_user_id,

       -- Audit Details
       u1.username
           AS created_by,
       ft.created_at,

       u2.username
           AS updated_by,
       ft.updated_at

FROM financial_transactions ft

         LEFT JOIN user_subcategories usc
                   ON usc.id = ft.subcategory_id

         LEFT JOIN user_categories uc
                   ON uc.id = usc.user_category_id

         LEFT JOIN user_reference_value transaction_type
                   ON transaction_type.id = uc.transaction_type
                       AND transaction_type.user_id = uc.user_id

         LEFT JOIN user_reference_value location
                   ON location.id = ft.location_id
                       AND location.user_id = uc.user_id

         LEFT JOIN users u1
                   ON u1.id = ft.created_by

         LEFT JOIN users u2
                   ON u2.id = ft.updated_by;