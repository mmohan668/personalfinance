CREATE OR REPLACE PROCEDURE insert_user_categories_bulk(
    IN p_user_id BIGINT,
    IN p_created_by BIGINT,
    IN p_rows JSONB
)
    LANGUAGE plpgsql
AS
$$
BEGIN

    INSERT INTO user_categories (user_id,
                                 transaction_type,
                                 category_name,
                                 category_description,
                                 created_by,
                                 created_at)
    SELECT p_user_id,
           rv.id,
           TRIM(x.category_name),
           TRIM(x.category_description),
           p_created_by,
           CURRENT_TIMESTAMP
    FROM jsonb_to_recordset(p_rows) AS x(
                                         transaction_type TEXT,
                                         category_name TEXT,
                                         category_description TEXT
        )
             JOIN reference_value rv
                  ON rv.user_id = p_user_id
                      AND LOWER(TRIM(rv.reference_code))
                         = LOWER(TRIM(x.transaction_type));

END;
$$;