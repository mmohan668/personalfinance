CREATE
    OR REPLACE PROCEDURE insert_user_categories_bulk(
    IN p_user_id BIGINT,
    IN p_created_by BIGINT,
    IN p_rows JSONB
)
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_input_count BIGINT;
    v_invalid_count
                  BIGINT;
    v_transaction_type_ref_id
                  BIGINT;
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
            'No category records were provided';
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
                     x.category_description
              FROM jsonb_to_recordset(p_rows) AS x
                       (
                        transaction_type TEXT,
                        category_name TEXT,
                        category_description TEXT
                           ))
    SELECT COUNT(*)
    INTO v_invalid_count
    FROM input_rows x
    WHERE NULLIF(TRIM(x.transaction_type), '') IS NULL
       OR NULLIF(TRIM(x.category_name), '') IS NULL
       OR NULLIF(TRIM(x.category_description), '') IS NULL

       OR NOT EXISTS
        (SELECT 1
         FROM user_reference_value urv
         WHERE urv.user_id = p_user_id
           AND urv.ref_obj_name_id = v_transaction_type_ref_id
           AND LOWER(TRIM(urv.reference_code)) =
               LOWER(TRIM(x.transaction_type))
           AND urv.is_active = TRUE);

    IF
        v_invalid_count > 0 THEN
        RAISE EXCEPTION
            'Category validation failed. % invalid record(s) found out of %.',
            v_invalid_count,
            v_input_count;
    END IF;

    INSERT INTO user_categories
    (user_id,
     transaction_type,
     category_name,
     category_description,
     is_active,
     created_by,
     created_at)
    SELECT p_user_id,
           urv.id,
           TRIM(x.category_name),
           TRIM(x.category_description),
           TRUE,
           p_created_by,
           CURRENT_TIMESTAMP

    FROM jsonb_to_recordset(p_rows) AS x
             (
              transaction_type TEXT,
              category_name TEXT,
              category_description TEXT
                 )

             JOIN user_reference_value urv
                  ON urv.user_id = p_user_id
                      AND urv.ref_obj_name_id = v_transaction_type_ref_id
                      AND LOWER(TRIM(urv.reference_code)) =
                          LOWER(TRIM(x.transaction_type))
                      AND urv.is_active = TRUE
    ON CONFLICT ( user_id, transaction_type, (LOWER(TRIM(category_name))) )
    WHERE is_deleted = FALSE DO NOTHING;

END;
$$;