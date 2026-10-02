INSERT INTO excel_validator_metadata
(upload_type,
 sheet_name,
 column_name,
 column_index,
 data_type,
 required,
 db_validation_type,
 db_validation_query,
 db_validation_columns,
 created_by)
VALUES ('Reference Values',
        'Reference Values',
        'Reference Object',
        0,
        'TEXT',
        true,
        'DB_EXISTS',
        'SELECT EXISTS(' ||
        'SELECT 1 FROM reference_object ro ' ||
        'WHERE LOWER(TRIM(ro.ref_obj_name)) = ?' ||
        ')',
        'Reference Object',
        (SELECT id FROM users WHERE username = 'PERSONALFINANCEAPP'));

INSERT INTO excel_validator_metadata
(upload_type,
 sheet_name,
 column_name,
 column_index,
 data_type,
 required,
 min_length,
 max_length,
 db_validation_type,
 db_validation_query,
 db_validation_columns,
 created_by)
VALUES ('Reference Values',
        'Reference Values',
        'Reference Code',
        1,
        'TEXT',
        true,
        3,
        100,
        'DB_DUPLICATE',
        'SELECT EXISTS(' ||
        'SELECT 1 FROM reference_value rv ' ||
        'JOIN reference_object ro ON ro.id = rv.ref_obj_name_id ' ||
        'WHERE LOWER(TRIM(ro.ref_obj_name)) = ? ' ||
        'AND LOWER(TRIM(rv.reference_code)) = ?)',
        'Reference Object, Reference Code',
        (SELECT id FROM users WHERE username = 'PERSONALFINANCEAPP'));

INSERT INTO excel_validator_metadata
(upload_type,
 sheet_name,
 column_name,
 column_index,
 data_type,
 required,
 min_length,
 max_length,
 created_by)
VALUES ('Reference Values',
        'Reference Values',
        'Reference Code Description',
        2,
        'TEXT',
        true,
        3,
        200,
        (SELECT id FROM users WHERE username = 'PERSONALFINANCEAPP'));

INSERT INTO excel_validator_metadata
(upload_type,
 sheet_name,
 column_name,
 column_index,
 data_type,
 max_length,
 created_by)
VALUES ('Reference Values',
        'Reference Values',
        'Reference Code 2',
        3,
        'TEXT',
        100,
        (SELECT id FROM users WHERE username = 'PERSONALFINANCEAPP'));

INSERT INTO excel_validator_metadata
(upload_type,
 sheet_name,
 column_name,
 column_index,
 data_type,
 max_length,
 created_by)
VALUES ('Reference Values',
        'Reference Values',
        'Reference Code 3',
        4,
        'TEXT',
        100,
        (SELECT id FROM users WHERE username = 'PERSONALFINANCEAPP'));