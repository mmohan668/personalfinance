INSERT INTO template_name(template_name, sheet_name, created_by)
VALUES ('CATEGORIES', 'CATEGORIES',
        (SELECT u.id FROM users u WHERE u.username = 'PERSONALFINANCEAPP'));

INSERT INTO template_header
(template_name_id,
 header_name,
 column_index,
 data_type,
 required,
 db_validation_type,
 db_validation_query,
 db_validation_columns,
 description,
 created_by)
VALUES ((SELECT tn.id FROM template_name tn WHERE tn.template_name = 'CATEGORIES'),
        'Transaction Type',
        0,
        'TEXT',
        true,
        'DB_EXISTS',
        'SELECT EXISTS (SELECT 1 FROM reference_value rv JOIN reference_object ro ON ro.id = rv.ref_obj_name_id WHERE LOWER(TRIM(rv.reference_code)) = ? AND LOWER(TRIM(ro.ref_obj_name)) = ''transaction_type'')',
        'Transaction Type',
        'Reference value. Enter a valid transaction type from the ''Txn Types'' sheet.',
        (SELECT u.id FROM users u WHERE u.username = 'PERSONALFINANCEAPP'));

INSERT INTO template_header
(template_name_id,
 header_name,
 column_index,
 data_type,
 required,
 min_length,
 max_length,
 db_validation_type,
 db_validation_query,
 db_validation_columns,
 description,
 created_by)
VALUES ((SELECT tn.id FROM template_name tn WHERE tn.template_name = 'CATEGORIES'),
        'Category Name',
        1,
        'TEXT',
        true,
        3,
        100,
        'DB_DUPLICATE',
        'SELECT EXISTS(SELECT 1 FROM user_categories uc JOIN reference_value rv ON uc.transaction_type = rv.id JOIN reference_object ro ON ro.id = rv.ref_obj_name_id WHERE LOWER(TRIM(ro.ref_obj_name)) = ''transaction_type'' AND LOWER(TRIM(rv.reference_code)) = ? AND LOWER(TRIM(uc.category_name)) = ?)',
        'Transaction Type, Category Name',
        'Category name associated with the selected reference object.',
        (SELECT u.id FROM users u WHERE u.username = 'PERSONALFINANCEAPP'));

INSERT INTO template_header
(template_name_id,
 header_name,
 column_index,
 data_type,
 required,
 min_length,
 max_length,
 description,
 created_by)
VALUES ((SELECT tn.id FROM template_name tn WHERE tn.template_name = 'CATEGORIES'),
        'Category Description',
        2,
        'TEXT',
        true,
        3,
        200,
        'Description of the category.',
        (SELECT u.id FROM users u WHERE u.username = 'PERSONALFINANCEAPP'));

INSERT INTO template_additional_sheet
(template_name_id,
 sheet_name,
 data_reference,
 created_by)
VALUES ((SELECT tn.id FROM template_name tn WHERE tn.template_name = 'CATEGORIES'),
        'Txn Types',
        'TRANSACTION_TYPE',
        (SELECT u.id FROM users u WHERE u.username = 'PERSONALFINANCEAPP'));