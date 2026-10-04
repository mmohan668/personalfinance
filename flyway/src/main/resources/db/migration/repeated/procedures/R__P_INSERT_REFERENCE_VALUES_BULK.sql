CREATE OR REPLACE PROCEDURE insert_reference_values_bulk(
    IN p_user_id BIGINT,
    IN p_created_by BIGINT,
    IN p_rows JSONB
)
    LANGUAGE plpgsql
AS
$$
BEGIN

    INSERT INTO reference_value (user_id,
                                 ref_obj_name_id,
                                 reference_code,
                                 reference_code_description,
                                 reference_code_2,
                                 reference_code_3,
                                 created_by,
                                 created_at)
    SELECT p_user_id,
           ro.id,
           TRIM(x.reference_code),
           TRIM(x.reference_code_description),
           NULLIF(TRIM(x.reference_code_2), ''),
           NULLIF(TRIM(x.reference_code_3), ''),
           p_created_by,
           CURRENT_TIMESTAMP
    FROM jsonb_to_recordset(p_rows) AS x(
                                         reference_object TEXT,
                                         reference_code TEXT,
                                         reference_code_description TEXT,
                                         reference_code_2 TEXT,
                                         reference_code_3 TEXT
        )
             JOIN reference_object ro
                  ON ro.user_id = p_user_id
                      AND LOWER(TRIM(ro.ref_obj_name))
                         = LOWER(TRIM(x.reference_object));

END;
$$;