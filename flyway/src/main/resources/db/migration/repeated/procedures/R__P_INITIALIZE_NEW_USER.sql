CREATE OR REPLACE PROCEDURE initialize_new_user(
    IN p_user_id BIGINT
)
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_created_by       BIGINT;
    v_currency_code_id BIGINT;
BEGIN

    /*
     * 1. Validate user
     */
    IF NOT EXISTS (SELECT 1
                   FROM users
                   WHERE id = p_user_id) THEN
        RAISE EXCEPTION 'User with id % does not exist', p_user_id;
    END IF;


    /*
     * 2. Get system/admin user
     *
     * This user is used as created_by for
     * default/master data copied to the new user.
     */
    SELECT id
    INTO v_created_by
    FROM users
    WHERE username = 'PERSONALFINANCEAPP';

    IF v_created_by IS NULL THEN
        RAISE EXCEPTION
            'System user PERSONALFINANCEAPP does not exist';
    END IF;


    /*
     * 3. Associate the new user with the admin/system user
     */
    UPDATE users
    SET admin_user_id = p_user_id
    WHERE id = p_user_id;


    INSERT INTO reference_object(ref_obj_name, user_id, created_by, created_at)
    SELECT sro.ref_obj_name,
           p_user_id,
           v_created_by,
           current_timestamp
    FROM system_reference_object sro
    ON CONFLICT (LOWER(TRIM(ref_obj_name)), user_id)
        DO NOTHING;

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
           srv.reference_code,
           srv.reference_code_description,
           srv.reference_code_2,
           srv.reference_code_3,
           v_created_by,
           CURRENT_TIMESTAMP
    FROM system_reference_value srv
             JOIN system_reference_object sro
                  ON sro.id = srv.ref_obj_name_id
             JOIN reference_object ro
                  ON ro.user_id = p_user_id
                      AND LOWER(TRIM(ro.ref_obj_name))
                         = LOWER(TRIM(sro.ref_obj_name))
    ON CONFLICT (
        user_id,
        ref_obj_name_id,
        LOWER(TRIM(reference_code))
        )
        DO NOTHING;

    SELECT id
    INTO v_currency_code_id
    FROM reference_object ro
    WHERE ro.ref_obj_name = 'CURRENCY_CODE';
/*
 * 7. Set default currency = INR
 */
    INSERT INTO system_config (config_name,
                               config_value,
                               description,
                               user_id,
                               created_by)
    SELECT 'Currency',
           rv.id,
           'Default currency used to determine currency symbols and locale-specific date formatting for data displayed in grids.',
           p_user_id,
           v_created_by
    FROM reference_value rv
    WHERE rv.user_id = p_user_id
      AND rv.ref_obj_name_id = v_currency_code_id
      AND LOWER(TRIM(rv.reference_code)) = 'inr'
    LIMIT 1
    ON CONFLICT (
        user_id,
        LOWER(TRIM(config_name))
        )
        DO NOTHING;


/*
 * 8. Copy master categories to user categories
 */
    INSERT INTO user_categories (user_id,
                                 transaction_type,
                                 category_name,
                                 category_description,
                                 is_active,
                                 created_by)
    SELECT p_user_id,
           c.transaction_type,
           c.category_name,
           c.category_description,
           c.is_active,
           v_created_by
    FROM categories c
    ON CONFLICT (
        user_id,
        transaction_type,
        LOWER(TRIM(category_name))
        )
        DO NOTHING;


/*
 * 9. Copy master subcategories to user subcategories
 */
    INSERT INTO user_subcategories (user_category_id,
                                    subcategory_name,
                                    subcategory_description,
                                    is_active,
                                    created_by)
    SELECT uc.id,
           sc.subcategory_name,
           sc.subcategory_description,
           sc.is_active,
           v_created_by
    FROM subcategories sc
             JOIN categories c
                  ON c.id = sc.category_id
             JOIN user_categories uc
                  ON uc.user_id = p_user_id
                      AND uc.transaction_type = c.transaction_type
                      AND LOWER(TRIM(uc.category_name))
                         = LOWER(TRIM(c.category_name))
    ON CONFLICT (
        user_category_id,
        LOWER(TRIM(subcategory_name))
        )
        DO NOTHING;


/*
 * Everything succeeded.
 *
 * Do not COMMIT here.
 * Let the caller/Spring transaction manage commit.
 */

END;
$$;