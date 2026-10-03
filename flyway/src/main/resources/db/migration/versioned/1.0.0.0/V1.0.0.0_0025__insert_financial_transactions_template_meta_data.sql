INSERT INTO template_name(template_name, sheet_name, created_by)
VALUES ('FINANCIAL_TRANSACTIONS', 'FINANCIAL_TRANSACTIONS',
        (SELECT u.id FROM users u WHERE u.username = 'PERSONALFINANCEAPP'));

INSERT INTO template_header
(template_name_id,
 header_name,
 column_index,
 data_type,
 required,
 description,
 created_by)
VALUES ((SELECT tn.id FROM template_name tn WHERE tn.template_name = 'FINANCIAL_TRANSACTIONS'),
        'Transaction Date',
        0,
        'DATE',
        true,
        'Transaction date. Enter a valid transaction date in the format dd-MM-yyyy.',
        (SELECT u.id FROM users u WHERE u.username = 'PERSONALFINANCEAPP'));

INSERT INTO template_header
(template_name_id,
 header_name,
 column_index,
 data_type,
 required,
 description,
 created_by)
VALUES ((SELECT tn.id FROM template_name tn WHERE tn.template_name = 'FINANCIAL_TRANSACTIONS'),
        'Amount',
        1,
        'AMOUNT',
        true,
        'Transaction amount. Enter a valid amount. Both positive and negative values are allowed.',
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
VALUES ((SELECT tn.id FROM template_name tn WHERE tn.template_name = 'FINANCIAL_TRANSACTIONS'),
        'Transaction Type',
        2,
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
VALUES ((SELECT tn.id FROM template_name tn WHERE tn.template_name = 'FINANCIAL_TRANSACTIONS'),
        'Category Name',
        3,
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
 db_validation_type,
 db_validation_query,
 db_validation_columns,
 description,
 created_by)
VALUES ((SELECT tn.id FROM template_name tn WHERE tn.template_name = 'FINANCIAL_TRANSACTIONS'),
        'Subcategory Name',
        4,
        'TEXT',
        true,
        'DB_EXISTS',
        'SELECT EXISTS(SELECT 1 FROM user_subcategories usc JOIN user_categories uc ON uc.id = usc.user_category_id JOIN reference_value rv ON rv.id = uc.transaction_type JOIN reference_object ro ON ro.id = rv.ref_obj_name_id WHERE LOWER(TRIM(ro.ref_obj_name)) = ''transaction_type'' AND LOWER(TRIM(rv.reference_code)) = ? AND LOWER(TRIM(uc.category_name)) = ? AND LOWER(TRIM(usc.subcategory_name)) = ?)',
        'Transaction Type, Category Name, Subcategory Name',
        'Subcategory associated with the transaction. Enter a valid subcategory from the ''Subcategories'' sheet that belongs to the selected category.',
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
VALUES ((SELECT tn.id FROM template_name tn WHERE tn.template_name = 'FINANCIAL_TRANSACTIONS'),
        'Location',
        5,
        'TEXT',
        false,
        'DB_EXISTS',
        'SELECT EXISTS (SELECT 1 FROM reference_value rv JOIN reference_object ro ON ro.id = rv.ref_obj_name_id WHERE LOWER(TRIM(rv.reference_code)) = ? AND LOWER(TRIM(ro.ref_obj_name)) = ''location'')',
        'Location',
        'Location where the financial transaction occurred. Enter a valid Location value from the ''Locations'' sheet. Use the value from the ''Location'' column; the ''Description'' column is provided for reference only.',
        (SELECT u.id FROM users u WHERE u.username = 'PERSONALFINANCEAPP'));

INSERT INTO template_header
(template_name_id,
 header_name,
 column_index,
 data_type,
 required,
 description,
 created_by)
VALUES ((SELECT tn.id FROM template_name tn WHERE tn.template_name = 'FINANCIAL_TRANSACTIONS'),
        'Remarks',
        6,
        'TEXT',
        false,
        'Location where the financial transaction occurred. Enter a valid Location value from the ''Locations'' sheet. Use the value from the ''Location'' column; the ''Description'' column is provided for reference only.',
        (SELECT u.id FROM users u WHERE u.username = 'PERSONALFINANCEAPP'));

INSERT INTO template_additional_sheet
(template_name_id,
 sheet_name,
 data_reference,
 created_by)
VALUES ((SELECT tn.id FROM template_name tn WHERE tn.template_name = 'FINANCIAL_TRANSACTIONS'),
        'Txn Types',
        'TRANSACTION_TYPE',
        (SELECT u.id FROM users u WHERE u.username = 'PERSONALFINANCEAPP'));

INSERT INTO template_additional_sheet
(template_name_id,
 sheet_name,
 data_reference,
 created_by)
VALUES ((SELECT tn.id FROM template_name tn WHERE tn.template_name = 'FINANCIAL_TRANSACTIONS'),
        'Categories',
        'CATEGORY',
        (SELECT u.id FROM users u WHERE u.username = 'PERSONALFINANCEAPP'));

INSERT INTO template_additional_sheet
(template_name_id,
 sheet_name,
 data_reference,
 created_by)
VALUES ((SELECT tn.id FROM template_name tn WHERE tn.template_name = 'FINANCIAL_TRANSACTIONS'),
        'Subcategories',
        'SUB_CATEGORY',
        (SELECT u.id FROM users u WHERE u.username = 'PERSONALFINANCEAPP'));

INSERT INTO template_additional_sheet
(template_name_id,
 sheet_name,
 data_reference,
 created_by)
VALUES ((SELECT tn.id FROM template_name tn WHERE tn.template_name = 'FINANCIAL_TRANSACTIONS'),
        'Locations',
        'LOCATION',
        (SELECT u.id FROM users u WHERE u.username = 'PERSONALFINANCEAPP'));