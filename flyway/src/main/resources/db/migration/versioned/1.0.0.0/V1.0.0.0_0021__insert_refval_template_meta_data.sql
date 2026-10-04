INSERT INTO template_name
(db_table_name,
 template_name,
 sheet_name,
 created_by)
VALUES ('reference_value',
        'REFERENCE_VALUES',
        'REFERENCE_VALUES',
        (SELECT u.id FROM users u WHERE u.username = 'PERSONALFINANCEAPP'));

INSERT INTO template_header
(db_column_name,
 template_name_id,
 header_name,
 column_index,
 data_type,
 required,
 db_validation_type,
 db_validation_query,
 db_validation_columns,
 description,
 created_by)
VALUES ('reference_object',
        (SELECT tn.id FROM template_name tn WHERE tn.template_name = 'REFERENCE_VALUES'),
        'Reference Object',
        0,
        'TEXT',
        true,
        'DB_EXISTS',
        'SELECT EXISTS(SELECT 1 FROM reference_object ro WHERE LOWER(TRIM(ro.ref_obj_name)) = ?)',
        'Reference Object',
        'Reference object name. Enter a valid reference object name from the ''Ref Obj'' sheet.',
        (SELECT u.id FROM users u WHERE u.username = 'PERSONALFINANCEAPP'));

INSERT INTO template_header
(db_column_name,
 template_name_id,
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
VALUES ('reference_code',
        (SELECT tn.id FROM template_name tn WHERE tn.template_name = 'REFERENCE_VALUES'),
        'Reference Code',
        1,
        'TEXT',
        true,
        3,
        100,
        'DB_DUPLICATE',
        'SELECT EXISTS(SELECT 1 FROM reference_value rv JOIN reference_object ro ON ro.id = rv.ref_obj_name_id WHERE LOWER(TRIM(ro.ref_obj_name)) = ? AND LOWER(TRIM(rv.reference_code)) = ?)',
        'Reference Object, Reference Code',
        'Reference code associated with the selected reference object.',
        (SELECT u.id FROM users u WHERE u.username = 'PERSONALFINANCEAPP'));

INSERT INTO template_header
(db_column_name,
 template_name_id,
 header_name,
 column_index,
 data_type,
 required,
 min_length,
 max_length,
 description,
 created_by)
VALUES ('reference_code_description',
        (SELECT tn.id FROM template_name tn WHERE tn.template_name = 'REFERENCE_VALUES'),
        'Reference Code Description',
        2,
        'TEXT',
        true,
        3,
        200,
        'Description of the reference code.',
        (SELECT u.id FROM users u WHERE u.username = 'PERSONALFINANCEAPP'));

INSERT INTO template_header
(db_column_name,
 template_name_id,
 header_name,
 column_index,
 data_type,
 required,
 description,
 created_by)
VALUES ('reference_code_2',
        (SELECT tn.id FROM template_name tn WHERE tn.template_name = 'REFERENCE_VALUES'),
        'Reference Code 2',
        3,
        'TEXT',
        false,
        'Optional secondary reference code.',
        (SELECT u.id FROM users u WHERE u.username = 'PERSONALFINANCEAPP'));

INSERT INTO template_header
(db_column_name,
 template_name_id,
 header_name,
 column_index,
 data_type,
 required,
 description,
 created_by)
VALUES ('reference_code_3',
        (SELECT tn.id FROM template_name tn WHERE tn.template_name = 'REFERENCE_VALUES'),
        'Reference Code 3',
        4,
        'TEXT',
        false,
        'Optional tertiary reference code.',
        (SELECT u.id FROM users u WHERE u.username = 'PERSONALFINANCEAPP'));

INSERT INTO template_additional_sheet
(template_name_id,
 sheet_name,
 data_reference,
 created_by)
VALUES ((SELECT tn.id FROM template_name tn WHERE tn.template_name = 'REFERENCE_VALUES'),
        'Ref Obj',
        'REFERENCE_OBJECT',
        (SELECT u.id FROM users u WHERE u.username = 'PERSONALFINANCEAPP'));