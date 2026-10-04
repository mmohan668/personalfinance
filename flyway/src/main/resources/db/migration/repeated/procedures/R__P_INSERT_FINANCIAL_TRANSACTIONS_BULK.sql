CREATE OR REPLACE PROCEDURE insert_financial_transactions_bulk(
    IN p_user_id BIGINT,
    IN p_created_by BIGINT,
    IN p_rows JSONB
)
    LANGUAGE plpgsql
AS
$$
BEGIN

    INSERT INTO financial_transactions (amount,
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
           rv.id,
           TRIM(x.remarks),
           p_user_id,
           p_created_by,
           CURRENT_TIMESTAMP
    FROM jsonb_to_recordset(p_rows) AS x(transaction_at DATE,
                                         amount NUMERIC,
                                         subcategory_name TEXT,
                                         location TEXT,
                                         remarks TEXT,
                                         transaction_type TEXT
        )
             JOIN user_subcategories usc
                  ON LOWER(TRIM(usc.subcategory_name)) = LOWER(TRIM(x.subcategory_name))
             JOIN user_categories uc
                  ON uc.user_id = p_user_id
                      AND uc.id = usc.user_category_id
             LEFT JOIN reference_value rv
                       ON rv.user_id = p_user_id
                           AND LOWER(TRIM(rv.reference_code)) = LOWER(TRIM(x.location))
                           AND rv.ref_obj_name_id = (SELECT id FROM reference_object WHERE ref_obj_name = 'LOCATION');
END;
$$;