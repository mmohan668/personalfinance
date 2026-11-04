-- ============================================================
-- FINANCIAL TRANSACTIONS TEMPLATE CONFIGURATION
-- ============================================================

DO
$$
    DECLARE
        v_system_user_id   BIGINT;
        v_template_name_id BIGINT;
    BEGIN

        -- ========================================================
        -- 1. VALIDATE SYSTEM USER
        -- ========================================================

        SELECT u.id
        INTO v_system_user_id
        FROM users u
        WHERE LOWER(TRIM(u.username)) = LOWER(TRIM('System'))
          AND u.is_deleted = FALSE;

        IF v_system_user_id IS NULL THEN
            RAISE EXCEPTION
                'Active System user does not exist. Cannot configure FINANCIAL_TRANSACTIONS template.';
        END IF;


        -- ========================================================
        -- 2. TEMPLATE NAME
        -- ========================================================

        INSERT INTO template_name
        (db_table_name,
         template_name,
         sheet_name,
         created_by)
        VALUES ('financial_transactions',
                'FINANCIAL_TRANSACTIONS',
                'FINANCIAL_TRANSACTIONS',
                v_system_user_id)
        ON CONFLICT (LOWER(TRIM(template_name)))
            DO NOTHING;


        SELECT tn.id
        INTO v_template_name_id
        FROM template_name tn
        WHERE LOWER(TRIM(tn.template_name)) =
              LOWER(TRIM('FINANCIAL_TRANSACTIONS'));

        IF v_template_name_id IS NULL THEN
            RAISE EXCEPTION
                'Unable to retrieve FINANCIAL_TRANSACTIONS template configuration.';
        END IF;


        -- ========================================================
        -- 3. TEMPLATE HEADERS
        -- ========================================================

        -- --------------------------------------------------------
        -- 3.1 TRANSACTION DATE
        -- --------------------------------------------------------

        INSERT INTO template_header
        (db_column_name,
         template_name_id,
         header_name,
         column_index,
         data_type,
         required,
         description,
         created_by)
        VALUES ('transaction_at',
                v_template_name_id,
                'Transaction Date',
                0,
                'DATE',
                TRUE,
                'Transaction date. Enter a valid transaction date using the date format configured in your user preferences.',
                v_system_user_id)
        ON CONFLICT (template_name_id, LOWER(TRIM(header_name)))
            DO NOTHING;


        -- --------------------------------------------------------
        -- 3.2 AMOUNT
        -- --------------------------------------------------------

        INSERT INTO template_header
        (db_column_name,
         template_name_id,
         header_name,
         column_index,
         data_type,
         required,
         description,
         created_by)
        VALUES ('amount',
                v_template_name_id,
                'Amount',
                1,
                'AMOUNT',
                TRUE,
                'Transaction amount. Enter a valid amount. Both positive and negative values are allowed.',
                v_system_user_id)
        ON CONFLICT (template_name_id, LOWER(TRIM(header_name)))
            DO NOTHING;


        -- --------------------------------------------------------
        -- 3.3 TRANSACTION TYPE
        -- --------------------------------------------------------

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
                2,
                'TEXT',
                TRUE,
                'DB_EXISTS',
                'SELECT EXISTS (
                    SELECT 1
                    FROM user_reference_value urv
                    JOIN reference_object ro
                        ON ro.id = urv.ref_obj_name_id
                    WHERE LOWER(TRIM(urv.reference_code)) = ?
                      AND LOWER(TRIM(ro.ref_obj_name)) = ''transaction_type''
                      AND urv.user_id = ?
                      AND urv.is_active = TRUE
                )',
                'Transaction Type',
                'Transaction type. Enter a valid transaction type from the ''Txn Types'' sheet.',
                v_system_user_id)
        ON CONFLICT (template_name_id, LOWER(TRIM(header_name)))
            DO NOTHING;


        -- --------------------------------------------------------
        -- 3.4 CATEGORY NAME
        -- --------------------------------------------------------

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
                3,
                'TEXT',
                TRUE,
                'DB_EXISTS',
                'SELECT EXISTS (
                    SELECT 1
                    FROM user_categories uc
                    JOIN user_reference_value urv
                        ON urv.id = uc.transaction_type
                    JOIN reference_object ro
                        ON ro.id = urv.ref_obj_name_id
                    WHERE LOWER(TRIM(ro.ref_obj_name)) = ''transaction_type''
                      AND LOWER(TRIM(urv.reference_code)) = ?
                      AND LOWER(TRIM(uc.category_name)) = ?
                      AND uc.user_id = ?
                      AND uc.is_active = TRUE
                )',
                'Transaction Type, Category Name',
                'Category associated with the selected transaction type. Enter a valid category from the ''Categories'' sheet.',
                v_system_user_id)
        ON CONFLICT (template_name_id, LOWER(TRIM(header_name)))
            DO NOTHING;


        -- --------------------------------------------------------
        -- 3.5 SUBCATEGORY NAME
        -- --------------------------------------------------------

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
        VALUES ('subcategory_name',
                v_template_name_id,
                'Subcategory Name',
                4,
                'TEXT',
                TRUE,
                'DB_EXISTS',
                'SELECT EXISTS (
                    SELECT 1
                    FROM user_subcategories usc
                    JOIN user_categories uc
                        ON uc.id = usc.user_category_id
                    JOIN user_reference_value urv
                        ON urv.id = uc.transaction_type
                    JOIN reference_object ro
                        ON ro.id = urv.ref_obj_name_id
                    WHERE LOWER(TRIM(ro.ref_obj_name)) = ''transaction_type''
                      AND LOWER(TRIM(urv.reference_code)) = ?
                      AND LOWER(TRIM(uc.category_name)) = ?
                      AND LOWER(TRIM(usc.subcategory_name)) = ?
                      AND uc.user_id = ?
                      AND uc.is_active = TRUE
                      AND usc.is_active = TRUE
                )',
                'Transaction Type, Category Name, Subcategory Name',
                'Subcategory associated with the transaction. Enter a valid subcategory from the ''Subcategories'' sheet that belongs to the selected category.',
                v_system_user_id)
        ON CONFLICT (template_name_id, LOWER(TRIM(header_name)))
            DO NOTHING;


        -- --------------------------------------------------------
        -- 3.6 LOCATION
        -- --------------------------------------------------------

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
        VALUES ('location',
                v_template_name_id,
                'Location',
                5,
                'TEXT',
                FALSE,
                'DB_EXISTS',
                'SELECT EXISTS (
                    SELECT 1
                    FROM user_reference_value urv
                    JOIN reference_object ro
                        ON ro.id = urv.ref_obj_name_id
                    WHERE LOWER(TRIM(urv.reference_code)) = ?
                      AND LOWER(TRIM(ro.ref_obj_name)) = ''location''
                      AND urv.user_id = ?
                      AND urv.is_active = TRUE
                )',
                'Location',
                'Location where the financial transaction occurred. Enter a valid Location value from the ''Locations'' sheet. Use the value from the ''Location'' column; the ''Description'' column is provided for reference only.',
                v_system_user_id)
        ON CONFLICT (template_name_id, LOWER(TRIM(header_name)))
            DO NOTHING;


        -- --------------------------------------------------------
        -- 3.7 REMARKS
        -- --------------------------------------------------------

        INSERT INTO template_header
        (db_column_name,
         template_name_id,
         header_name,
         column_index,
         data_type,
         required,
         description,
         created_by)
        VALUES ('remarks',
                v_template_name_id,
                'Remarks',
                6,
                'TEXT',
                FALSE,
                'Optional remarks or additional information about the financial transaction.',
                v_system_user_id)
        ON CONFLICT (template_name_id, LOWER(TRIM(header_name)))
            DO NOTHING;


        -- ========================================================
        -- 4. ADDITIONAL SHEET CONFIGURATION
        -- ========================================================

        -- --------------------------------------------------------
        -- 4.1 TRANSACTION TYPES
        -- --------------------------------------------------------

        INSERT INTO template_additional_sheet
        (template_name_id,
         sheet_name,
         data_reference,
         created_by)
        VALUES (v_template_name_id,
                'Txn Types',
                'TRANSACTION_TYPE',
                v_system_user_id)
        ON CONFLICT (template_name_id, LOWER(TRIM(sheet_name)))
            DO NOTHING;


        -- --------------------------------------------------------
        -- 4.2 CATEGORIES
        -- --------------------------------------------------------

        INSERT INTO template_additional_sheet
        (template_name_id,
         sheet_name,
         data_reference,
         created_by)
        VALUES (v_template_name_id,
                'Categories',
                'CATEGORY',
                v_system_user_id)
        ON CONFLICT (template_name_id, LOWER(TRIM(sheet_name)))
            DO NOTHING;


        -- --------------------------------------------------------
        -- 4.3 SUBCATEGORIES
        -- --------------------------------------------------------

        INSERT INTO template_additional_sheet
        (template_name_id,
         sheet_name,
         data_reference,
         created_by)
        VALUES (v_template_name_id,
                'Subcategories',
                'SUB_CATEGORY',
                v_system_user_id)
        ON CONFLICT (template_name_id, LOWER(TRIM(sheet_name)))
            DO NOTHING;


        -- --------------------------------------------------------
        -- 4.4 LOCATIONS
        -- --------------------------------------------------------

        INSERT INTO template_additional_sheet
        (template_name_id,
         sheet_name,
         data_reference,
         created_by)
        VALUES (v_template_name_id,
                'Locations',
                'LOCATION',
                v_system_user_id)
        ON CONFLICT (template_name_id, LOWER(TRIM(sheet_name)))
            DO NOTHING;

    END;
$$;