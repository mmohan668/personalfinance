INSERT INTO template_name(template_name, sheet_name, created_by)
VALUES ('SUBCATEGORIES', 'SUBCATEGORIES',
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
VALUES ((SELECT tn.id FROM template_name tn WHERE tn.template_name = 'SUBCATEGORIES'),
        'Transaction Type',
        0,
        'TEXT',
        true,
        'DB_EXISTS',
        'SELECT EXISTS (SELECT 1 FROM reference_value rv JOIN reference_object ro ON ro.id = rv.ref_obj_name_id WHERE LOWER(TRIM(rv.reference_code)) = ? AND LOWER(TRIM(ro.ref_obj_name)) = ''transaction_type'')',
        'Transaction Type',
        'Transaction type. Enter a valid transaction type from the ''Txn Types'' sheet.',
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
VALUES ((SELECT tn.id FROM template_name tn WHERE tn.template_name = 'SUBCATEGORIES'),
        'Category Name',
        1,
        'TEXT',
        true,
        'DB_EXISTS',
        'SELECT EXISTS(SELECT 1 FROM user_categories uc JOIN reference_value rv ON uc.transaction_type = rv.id JOIN reference_object ro ON ro.id = rv.ref_obj_name_id WHERE LOWER(TRIM(ro.ref_obj_name)) = ''transaction_type'' AND LOWER(TRIM(rv.reference_code)) = ? AND LOWER(TRIM(uc.category_name)) = ?)',
        'Transaction Type, Category Name',
        'Category associated with the subcategory. Enter a valid category from the ''Categories'' sheet.',
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
VALUES ((SELECT tn.id FROM template_name tn WHERE tn.template_name = 'SUBCATEGORIES'),
        'Subcategory Name',
        2,
        'TEXT',
        true,
        3,
        100,
        'DB_DUPLICATE',
        'SELECT EXISTS(SELECT 1 FROM user_subcategories usc JOIN user_categories uc ON uc.id = usc.user_category_id JOIN reference_value rv ON rv.id = uc.transaction_type JOIN reference_object ro ON ro.id = rv.ref_obj_name_id WHERE LOWER(TRIM(ro.ref_obj_name)) = ''transaction_type'' AND LOWER(TRIM(rv.reference_code)) = ? AND LOWER(TRIM(uc.category_name)) = ? AND LOWER(TRIM(usc.subcategory_name)) = ?)',
        'Transaction Type, Category Name, Subcategory Name',
        'Name of the subcategory.',
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
VALUES ((SELECT tn.id FROM template_name tn WHERE tn.template_name = 'SUBCATEGORIES'),
        'Subcategory Description',
        3,
        'TEXT',
        true,
        3,
        200,
        'Description of the subcategory.',
        (SELECT u.id FROM users u WHERE u.username = 'PERSONALFINANCEAPP'));

INSERT INTO template_additional_sheet
(template_name_id,
 sheet_name,
 data_reference,
 created_by)
VALUES ((SELECT tn.id FROM template_name tn WHERE tn.template_name = 'SUBCATEGORIES'),
        'Txn Types',
        'TRANSACTION_TYPE',
        (SELECT u.id FROM users u WHERE u.username = 'PERSONALFINANCEAPP'));

INSERT INTO template_additional_sheet
(template_name_id,
 sheet_name,
 data_reference,
 created_by)
VALUES ((SELECT tn.id FROM template_name tn WHERE tn.template_name = 'SUBCATEGORIES'),
        'Categories',
        'CATEGORY',
        (SELECT u.id FROM users u WHERE u.username = 'PERSONALFINANCEAPP'));