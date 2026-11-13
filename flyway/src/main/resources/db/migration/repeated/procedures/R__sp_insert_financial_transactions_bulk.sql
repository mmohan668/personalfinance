CREATE
    OR REPLACE PROCEDURE insert_financial_transactions_bulk(
    IN p_user_id BIGINT,
    IN p_created_by BIGINT,
    IN p_rows JSONB
)
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_input_count             BIGINT;
    v_invalid_count           BIGINT;
    v_transaction_type_ref_id BIGINT;
    v_location_ref_id         BIGINT;
BEGIN

    IF
        p_user_id IS NULL OR p_created_by IS NULL THEN
        RAISE EXCEPTION
            'User ID and Created By are required';
    END IF;

    IF
        p_rows IS NULL OR jsonb_typeof(p_rows) <> 'array' THEN
        RAISE EXCEPTION
            'Input rows must be a valid JSON array';
    END IF;

    SELECT COUNT(*)
    INTO v_input_count
    FROM jsonb_array_elements(p_rows);

    IF
        v_input_count = 0 THEN
        RAISE EXCEPTION
            'No financial transaction records were provided';
    END IF;

    IF
        NOT EXISTS
            (SELECT 1
             FROM users
             WHERE id = p_user_id
               AND is_active = TRUE
               AND is_deleted = FALSE) THEN
        RAISE EXCEPTION
            'Active user with id % does not exist',
            p_user_id;
    END IF;

    SELECT id
    INTO v_transaction_type_ref_id
    FROM reference_object
    WHERE LOWER(TRIM(ref_obj_name)) = 'transaction_type';

    SELECT id
    INTO v_location_ref_id
    FROM reference_object
    WHERE LOWER(TRIM(ref_obj_name)) = 'location';

    IF
        v_transaction_type_ref_id IS NULL THEN
        RAISE EXCEPTION
            'TRANSACTION_TYPE reference object does not exist';
    END IF;

    IF
        v_location_ref_id IS NULL THEN
        RAISE EXCEPTION
            'LOCATION reference object does not exist';
    END IF;

    WITH transaction_rows AS
             (SELECT x.transaction_at,
                     x.amount,
                     x.transaction_type,
                     x.category_name,
                     x.subcategory_name,
                     x.location,
                     x.remarks
              FROM jsonb_to_recordset(p_rows) AS x
                       (
                        transaction_at DATE,
                        amount NUMERIC,
                        transaction_type TEXT,
                        category_name TEXT,
                        subcategory_name TEXT,
                        location TEXT,
                        remarks TEXT
                           ))
    SELECT COUNT(*)
    INTO v_invalid_count
    FROM transaction_rows x
    WHERE x.transaction_at IS NULL
       OR x.amount IS NULL
       OR x.amount = 0
       OR NULLIF(TRIM(x.transaction_type), '') IS NULL
       OR NULLIF(TRIM(x.category_name), '') IS NULL
       OR NULLIF(TRIM(x.subcategory_name), '') IS NULL

       OR NOT EXISTS
        (SELECT 1
         FROM user_reference_value urv
         WHERE urv.user_id = p_user_id
           AND urv.ref_obj_name_id = v_transaction_type_ref_id
           AND LOWER(TRIM(urv.reference_code)) =
               LOWER(TRIM(x.transaction_type))
           AND urv.is_active = TRUE)

       OR NOT EXISTS
        (SELECT 1
         FROM user_categories uc
                  JOIN user_reference_value urv
                       ON urv.id = uc.transaction_type
                           AND urv.user_id = uc.user_id
         WHERE uc.user_id = p_user_id
           AND uc.is_active = TRUE
           AND urv.is_active = TRUE
           AND urv.ref_obj_name_id = v_transaction_type_ref_id
           AND LOWER(TRIM(urv.reference_code)) =
               LOWER(TRIM(x.transaction_type))
           AND LOWER(TRIM(uc.category_name)) =
               LOWER(TRIM(x.category_name)))

       OR NOT EXISTS
        (SELECT 1
         FROM user_subcategories usc
                  JOIN user_categories uc
                       ON uc.id = usc.user_category_id
                  JOIN user_reference_value urv
                       ON urv.id = uc.transaction_type
                           AND urv.user_id = uc.user_id
         WHERE uc.user_id = p_user_id
           AND uc.is_active = TRUE
           AND usc.is_active = TRUE
           AND urv.is_active = TRUE
           AND urv.ref_obj_name_id = v_transaction_type_ref_id
           AND LOWER(TRIM(urv.reference_code)) =
               LOWER(TRIM(x.transaction_type))
           AND LOWER(TRIM(uc.category_name)) =
               LOWER(TRIM(x.category_name))
           AND LOWER(TRIM(usc.subcategory_name)) =
               LOWER(TRIM(x.subcategory_name)))

       OR (
        NULLIF(TRIM(x.location), '') IS NOT NULL
            AND NOT EXISTS
            (SELECT 1
             FROM user_reference_value location
             WHERE location.user_id = p_user_id
               AND location.ref_obj_name_id = v_location_ref_id
               AND LOWER(TRIM(location.reference_code)) =
                   LOWER(TRIM(x.location))
               AND location.is_active = TRUE)
        );

    IF
        v_invalid_count > 0 THEN
        RAISE EXCEPTION
            'Bulk upload validation failed. % invalid transaction record(s) found out of %.',
            v_invalid_count,
            v_input_count;
    END IF;

    INSERT INTO financial_transactions
    (amount,
     transaction_at,
     subcategory_id,
     location_id,
     remarks,
     admin_user_id,
     created_by,
     created_at)
    SELECT CASE
               WHEN UPPER(TRIM(x.transaction_type)) = 'EXPENSE'
                   THEN -ABS(x.amount)
               ELSE x.amount
               END,

           x.transaction_at,
           usc.id,
           location.id,
           NULLIF(TRIM(x.remarks), ''),
           p_user_id,
           p_created_by,
           CURRENT_TIMESTAMP

    FROM jsonb_to_recordset(p_rows) AS x
             (
              transaction_at DATE,
              amount NUMERIC,
              transaction_type TEXT,
              category_name TEXT,
              subcategory_name TEXT,
              location TEXT,
              remarks TEXT
                 )

             JOIN user_reference_value transaction_type
                  ON transaction_type.user_id = p_user_id
                      AND transaction_type.ref_obj_name_id =
                          v_transaction_type_ref_id
                      AND LOWER(TRIM(transaction_type.reference_code)) =
                          LOWER(TRIM(x.transaction_type))
                      AND transaction_type.is_active = TRUE

             JOIN user_categories uc
                  ON uc.user_id = p_user_id
                      AND uc.transaction_type = transaction_type.id
                      AND LOWER(TRIM(uc.category_name)) =
                          LOWER(TRIM(x.category_name))
                      AND uc.is_active = TRUE

             JOIN user_subcategories usc
                  ON usc.user_category_id = uc.id
                      AND LOWER(TRIM(usc.subcategory_name)) =
                          LOWER(TRIM(x.subcategory_name))
                      AND usc.is_active = TRUE

             LEFT JOIN user_reference_value location
                       ON location.user_id = p_user_id
                           AND location.ref_obj_name_id = v_location_ref_id
                           AND LOWER(TRIM(location.reference_code)) =
                               LOWER(TRIM(x.location))
                           AND location.is_active = TRUE;

END;
$$;