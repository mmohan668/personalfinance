CREATE OR REPLACE PROCEDURE insert_user_subcategories_bulk(
    IN p_user_id BIGINT,
    IN p_created_by BIGINT,
    IN p_rows JSONB
)
    LANGUAGE plpgsql
AS
$$
BEGIN

    INSERT INTO user_subcategories (user_category_id,
                                    subcategory_name,
                                    subcategory_description,
                                    created_by,
                                    created_at)
    SELECT uc.id,
           TRIM(x.subcategory_name),
           TRIM(x.subcategory_description),
           p_created_by,
           CURRENT_TIMESTAMP
    FROM jsonb_to_recordset(p_rows) AS x(
                                         category_name TEXT,
                                         subcategory_name TEXT,
                                         subcategory_description TEXT
        )
             JOIN user_categories uc
                  ON uc.user_id = p_user_id
                      AND LOWER(TRIM(uc.category_name))
                         = LOWER(TRIM(x.category_name));

END;
$$;