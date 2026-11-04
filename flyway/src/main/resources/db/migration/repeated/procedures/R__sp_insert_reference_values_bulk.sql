-- ============================================================
-- Create Bulk Insert Reference Values Procedure
-- ============================================================

CREATE OR REPLACE PROCEDURE insert_reference_values_bulk(
    IN p_user_id BIGINT,
    IN p_created_by BIGINT,
    IN p_rows JSONB
)
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_input_count   BIGINT;
    v_invalid_count BIGINT;
BEGIN

    -- ========================================================
    -- 1. VALIDATE INPUT
    -- ========================================================

    IF p_user_id IS NULL OR p_created_by IS NULL THEN
        RAISE EXCEPTION
            'User ID and Created By are required';
    END IF;

    IF p_rows IS NULL OR jsonb_typeof(p_rows) <> 'array' THEN
        RAISE EXCEPTION
            'Input rows must be a valid JSON array';
    END IF;

    SELECT COUNT(*)
    INTO v_input_count
    FROM jsonb_array_elements(p_rows);

    IF v_input_count = 0 THEN
        RAISE EXCEPTION
            'No reference value records were provided';
    END IF;


    -- ========================================================
    -- 2. VALIDATE USER
    -- ========================================================

    IF NOT EXISTS
        (SELECT 1
         FROM users
         WHERE id = p_user_id
           AND is_active = TRUE
           AND is_deleted = FALSE) THEN
        RAISE EXCEPTION
            'Active user with id % does not exist',
            p_user_id;
    END IF;


    -- ========================================================
    -- 3. VALIDATE REFERENCE VALUE ROWS
    -- ========================================================

    WITH input_rows AS
             (SELECT x.reference_object,
                     x.reference_code,
                     x.reference_code_description,
                     x.reference_code_2,
                     x.reference_code_3
              FROM jsonb_to_recordset(p_rows) AS x
                       (
                        reference_object TEXT,
                        reference_code TEXT,
                        reference_code_description TEXT,
                        reference_code_2 TEXT,
                        reference_code_3 TEXT
                           ))
    SELECT COUNT(*)
    INTO v_invalid_count
    FROM input_rows x
    WHERE NULLIF(TRIM(x.reference_object), '') IS NULL
       OR NULLIF(TRIM(x.reference_code), '') IS NULL
       OR NULLIF(TRIM(x.reference_code_description), '') IS NULL

       OR NOT EXISTS
        (SELECT 1
         FROM reference_object ro
         WHERE LOWER(TRIM(ro.ref_obj_name)) =
               LOWER(TRIM(x.reference_object)));

    IF v_invalid_count > 0 THEN
        RAISE EXCEPTION
            'Reference value validation failed. % invalid record(s) found out of %.',
            v_invalid_count,
            v_input_count;
    END IF;


    -- ========================================================
    -- 4. INSERT USER REFERENCE VALUES
    -- ========================================================

    INSERT INTO user_reference_value
    (user_id,
     ref_obj_name_id,
     reference_code,
     reference_code_description,
     is_active,
     reference_code_2,
     reference_code_3,
     created_by,
     created_at)
    SELECT p_user_id,
           ro.id,
           TRIM(x.reference_code),
           TRIM(x.reference_code_description),
           TRUE,
           NULLIF(TRIM(x.reference_code_2), ''),
           NULLIF(TRIM(x.reference_code_3), ''),
           p_created_by,
           CURRENT_TIMESTAMP

    FROM jsonb_to_recordset(p_rows) AS x
             (
              reference_object TEXT,
              reference_code TEXT,
              reference_code_description TEXT,
              reference_code_2 TEXT,
              reference_code_3 TEXT
                 )

             JOIN reference_object ro
                  ON LOWER(TRIM(ro.ref_obj_name)) =
                     LOWER(TRIM(x.reference_object))

    ON CONFLICT
        (
        ref_obj_name_id,
        (LOWER(TRIM(reference_code))),
        user_id
        )
        DO NOTHING;

END;
$$;