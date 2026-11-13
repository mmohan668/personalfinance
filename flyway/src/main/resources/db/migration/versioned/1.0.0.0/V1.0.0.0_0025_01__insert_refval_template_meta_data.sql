DO
$$
    DECLARE
        v_system_user_id BIGINT;
        v_template_name_id
                         BIGINT;
    BEGIN

        SELECT u.id
        INTO v_system_user_id
        FROM users u
        WHERE LOWER(TRIM(u.username)) = LOWER(TRIM('System'))
          AND u.is_deleted = FALSE;

        IF
            v_system_user_id IS NULL THEN
            RAISE EXCEPTION
                'Active System user does not exist. Cannot configure REFERENCE_VALUES template.';
        END IF;

        INSERT INTO template_name
        (db_table_name,
         template_name,
         sheet_name,
         created_by)
        VALUES ('reference_values',
                'REFERENCE_VALUES',
                'REFERENCE_VALUES',
                v_system_user_id)
        ON CONFLICT (LOWER(TRIM(template_name))) DO NOTHING;

        SELECT tn.id
        INTO v_template_name_id
        FROM template_name tn
        WHERE LOWER(TRIM(tn.template_name)) = LOWER(TRIM('REFERENCE_VALUES'));

        IF
            v_template_name_id IS NULL THEN
            RAISE EXCEPTION
                'Unable to retrieve REFERENCE_VALUES template configuration.';
        END IF;

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
                v_template_name_id,
                'Reference Object',
                0,
                'TEXT',
                TRUE,
                'DB_EXISTS',
                'SELECT EXISTS(SELECT 1 FROM reference_object ro WHERE LOWER(TRIM(ro.ref_obj_name)) = ?)',
                'Reference Object',
                'Reference object name. Enter a valid reference object name from the ''Ref Obj'' sheet.',
                v_system_user_id)
        ON CONFLICT (template_name_id, LOWER(TRIM(header_name))) DO NOTHING;

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
                v_template_name_id,
                'Reference Code',
                1,
                'TEXT',
                TRUE,
                3,
                100,
                'DB_DUPLICATE',
                'SELECT EXISTS(SELECT 1 FROM reference_values rv JOIN reference_object ro ON ro.id = rv.ref_obj_name_id WHERE LOWER(TRIM(ro.ref_obj_name)) = ? AND LOWER(TRIM(rv.reference_code)) = ?)',
                'Reference Object, Reference Code',
                'Reference code associated with the selected reference object.',
                v_system_user_id)
        ON CONFLICT (template_name_id, LOWER(TRIM(header_name))) DO NOTHING;

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
                v_template_name_id,
                'Reference Code Description',
                2,
                'TEXT',
                TRUE,
                3,
                200,
                'Description of the reference code.',
                v_system_user_id)
        ON CONFLICT (template_name_id, LOWER(TRIM(header_name))) DO NOTHING;

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
                v_template_name_id,
                'Reference Code 2',
                3,
                'TEXT',
                FALSE,
                'Optional secondary reference code.',
                v_system_user_id)
        ON CONFLICT (template_name_id, LOWER(TRIM(header_name))) DO NOTHING;

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
                v_template_name_id,
                'Reference Code 3',
                4,
                'TEXT',
                FALSE,
                'Optional tertiary reference code.',
                v_system_user_id)
        ON CONFLICT (template_name_id, LOWER(TRIM(header_name))) DO NOTHING;

        INSERT INTO template_additional_sheet
        (template_name_id,
         sheet_name,
         data_reference,
         created_by)
        VALUES (v_template_name_id,
                'Ref Obj',
                'REFERENCE_OBJECT',
                v_system_user_id)
        ON CONFLICT (template_name_id, LOWER(TRIM(sheet_name))) DO NOTHING;

    END;
$$;