CREATE
    OR REPLACE PROCEDURE insert_user_subcategories_bulk(
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
            'No subcategory records were provided';
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

    IF
        v_transaction_type_ref_id IS NULL THEN
        RAISE EXCEPTION
            'TRANSACTION_TYPE reference object does not exist';
    END IF;

    WITH input_rows AS
             (SELECT x.transaction_type,
                     x.category_name,
                     x.subcategory_name,
                     x.subcategory_description
              FROM jsonb_to_recordset(p_rows) AS x
                       (transaction_type TEXT,
                        category_name TEXT,
                        subcategory_name TEXT,
                        subcategory_description TEXT))
    SELECT COUNT(*)
    INTO v_invalid_count
    FROM input_rows x
    WHERE NULLIF(TRIM(x.transaction_type), '') IS NULL
       OR NULLIF(TRIM(x.category_name), '') IS NULL
       OR NULLIF(TRIM(x.subcategory_name), '') IS NULL
       OR NULLIF(TRIM(x.subcategory_description), '') IS NULL

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
               LOWER(TRIM(x.category_name)));

    IF
        v_invalid_count > 0 THEN
        RAISE EXCEPTION
            'Subcategory validation failed. % invalid record(s) found out of %.',
            v_invalid_count,
            v_input_count;
    END IF;

    INSERT INTO user_subcategories
    (user_category_id,
     subcategory_name,
     subcategory_description,
     is_active,
     created_by,
     created_at)
    SELECT uc.id,
           TRIM(x.subcategory_name),
           TRIM(x.subcategory_description),
           TRUE,
           p_created_by,
           CURRENT_TIMESTAMP

    FROM jsonb_to_recordset(p_rows) AS x
             (transaction_type TEXT,
              category_name TEXT,
              subcategory_name TEXT,
              subcategory_description TEXT)

             JOIN user_reference_value urv
                  ON urv.user_id = p_user_id
                      AND urv.ref_obj_name_id = v_transaction_type_ref_id
                      AND LOWER(TRIM(urv.reference_code)) =
                          LOWER(TRIM(x.transaction_type))
                      AND urv.is_active = TRUE

             JOIN user_categories uc
                  ON uc.user_id = p_user_id
                      AND uc.transaction_type = urv.id
                      AND LOWER(TRIM(uc.category_name)) =
                          LOWER(TRIM(x.category_name))
                      AND uc.is_active = TRUE
    ON CONFLICT ( user_category_id, (LOWER(TRIM(subcategory_name))) ) DO NOTHING;

END;
$$;