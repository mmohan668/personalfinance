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
                'Active System user does not exist. Cannot configure SUBCATEGORIES template.';
        END IF;

        INSERT INTO template_name
        (db_table_name,
         template_name,
         sheet_name,
         created_by)
        VALUES ('user_subcategories',
                'SUBCATEGORIES',
                'SUBCATEGORIES',
                v_system_user_id)
        ON CONFLICT (LOWER(TRIM(template_name))) DO NOTHING;

        SELECT tn.id
        INTO v_template_name_id
        FROM template_name tn
        WHERE LOWER(TRIM(tn.template_name)) = LOWER(TRIM('SUBCATEGORIES'));

        IF
            v_template_name_id IS NULL THEN
            RAISE EXCEPTION
                'Unable to retrieve SUBCATEGORIES template configuration.';
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
        VALUES ('transaction_type',
                v_template_name_id,
                'Transaction Type',
                0,
                'TEXT',
                TRUE,
                'DB_EXISTS',
                'SELECT EXISTS (SELECT 1 FROM user_reference_value urv JOIN reference_object ro ON ro.id = urv.ref_obj_name_id WHERE LOWER(TRIM(urv.reference_code)) = ? AND LOWER(TRIM(ro.ref_obj_name)) = ''transaction_type'' AND urv.user_id = ? AND urv.is_active = TRUE AND urv.is_deleted = FALSE);',
                'Transaction Type',
                'Transaction type. Enter a valid transaction type from the ''Txn Types'' sheet.',
                v_system_user_id)
        ON CONFLICT (template_name_id, LOWER(TRIM(header_name))) DO NOTHING;

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
        VALUES ('category_name',
                v_template_name_id,
                'Category Name',
                1,
                'TEXT',
                TRUE,
                'DB_EXISTS',
                'SELECT EXISTS (SELECT 1 FROM user_categories uc JOIN user_reference_value urv ON urv.id = uc.transaction_type JOIN reference_object ro ON ro.id = urv.ref_obj_name_id WHERE LOWER(TRIM(ro.ref_obj_name)) = ''transaction_type'' AND LOWER(TRIM(urv.reference_code)) = ? AND LOWER(TRIM(uc.category_name)) = ? AND uc.user_id = ? AND uc.is_active = TRUE AND uc.is_deleted = FALSE);',
                'Transaction Type, Category Name',
                'Category associated with the subcategory. Enter a valid category from the ''Categories'' sheet.',
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
        VALUES ('subcategory_name',
                v_template_name_id,
                'Subcategory Name',
                2,
                'TEXT',
                TRUE,
                3,
                100,
                'DB_DUPLICATE',
                'SELECT EXISTS (SELECT 1 FROM user_subcategories usc JOIN user_categories uc ON uc.id = usc.user_category_id JOIN user_reference_value urv ON urv.id = uc.transaction_type JOIN reference_object ro ON ro.id = urv.ref_obj_name_id WHERE LOWER(TRIM(ro.ref_obj_name)) = ''transaction_type'' AND LOWER(TRIM(urv.reference_code)) = ? AND LOWER(TRIM(uc.category_name)) = ? AND LOWER(TRIM(usc.subcategory_name)) = ? AND uc.user_id = ? AND usc.is_deleted = FALSE);',
                'Transaction Type, Category Name, Subcategory Name',
                'Name of the subcategory.',
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
        VALUES ('subcategory_description',
                v_template_name_id,
                'Subcategory Description',
                3,
                'TEXT',
                TRUE,
                3,
                200,
                'Description of the subcategory.',
                v_system_user_id)
        ON CONFLICT (template_name_id, LOWER(TRIM(header_name))) DO NOTHING;

        INSERT INTO template_additional_sheet
        (template_name_id,
         sheet_name,
         data_reference,
         created_by)
        VALUES (v_template_name_id,
                'Txn Types',
                'TRANSACTION_TYPE',
                v_system_user_id)
        ON CONFLICT (template_name_id, LOWER(TRIM(sheet_name))) DO NOTHING;

        INSERT INTO template_additional_sheet
        (template_name_id,
         sheet_name,
         data_reference,
         created_by)
        VALUES (v_template_name_id,
                'Categories',
                'CATEGORY',
                v_system_user_id)
        ON CONFLICT (template_name_id, LOWER(TRIM(sheet_name))) DO NOTHING;

    END;
$$;