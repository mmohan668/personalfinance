DROP VIEW IF EXISTS vw_recurring_transaction_rules;

CREATE OR REPLACE VIEW vw_recurring_transaction_rules AS
SELECT r.id,
       r.admin_user_id,
       r.created_by AS created_by_id,
       created_user.username AS created_by,
       r.created_at,
       updated_user.username AS updated_by,
       r.updated_at,
       r.subcategory_id,
       c.id AS category_id,
       r.location_id,
       location.reference_code AS location,
       rv.reference_code AS transaction_type,
       c.category_name,
       s.subcategory_name,
       r.amount,
       r.remarks,
       r.frequency,
       r.interval_value,
       r.day_of_week,
       r.day_of_month,
       r.month_of_year,
       r.monthly_pattern,
       r.start_date,
       r.end_date,
       r.execution_time,
       r.is_active,
       r.rule_name
FROM recurring_transaction_rule r
         JOIN user_subcategories s ON s.id = r.subcategory_id
         JOIN user_categories c ON c.id = s.user_category_id
         JOIN user_reference_value rv ON rv.id = c.transaction_type
         LEFT JOIN user_reference_value location ON location.id = r.location_id
         LEFT JOIN users created_user ON created_user.id = r.created_by
         LEFT JOIN users updated_user ON updated_user.id = r.updated_by;
