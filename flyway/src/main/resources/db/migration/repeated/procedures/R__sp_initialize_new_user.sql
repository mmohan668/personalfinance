CREATE OR REPLACE PROCEDURE initialize_new_user(
    IN p_user_id BIGINT
)
    LANGUAGE plpgsql
AS
$$
DECLARE
    -- Reference object IDs
    v_currency_ref_object_id    BIGINT;
    v_date_format_ref_object_id BIGINT;
    v_transaction_type_ref_id   BIGINT;
    v_country_ref_object_id     BIGINT;
    v_state_ref_object_id       BIGINT;
    v_time_zone_ref_object_id   BIGINT;
    v_password_history_ref_id   BIGINT;

    -- User reference value IDs
    v_currency_code_id          BIGINT;
    v_date_format_id            BIGINT;
    v_country_code_id           BIGINT;
    v_state_code_id             BIGINT;
    v_time_zone_id              BIGINT;
    v_psd_his_limit_id          BIGINT;

BEGIN

    ----------------------------------------------------------------
    -- 1. Validate user
    ----------------------------------------------------------------

    IF p_user_id IS NULL THEN
        RAISE EXCEPTION 'User ID is required';
    END IF;

    IF NOT EXISTS (SELECT 1
                   FROM users
                   WHERE id = p_user_id
                     AND is_deleted = FALSE) THEN
        RAISE EXCEPTION
            'User with id % does not exist or is deleted',
            p_user_id;
    END IF;


    ----------------------------------------------------------------
    -- 2. Resolve required reference object IDs
    ----------------------------------------------------------------

    SELECT id
    INTO v_currency_ref_object_id
    FROM reference_object
    WHERE LOWER(TRIM(ref_obj_name)) = 'currency_code';

    IF v_currency_ref_object_id IS NULL THEN
        RAISE EXCEPTION
            'Reference object CURRENCY_CODE does not exist';
    END IF;


    SELECT id
    INTO v_date_format_ref_object_id
    FROM reference_object
    WHERE LOWER(TRIM(ref_obj_name)) = 'date_format';

    IF v_date_format_ref_object_id IS NULL THEN
        RAISE EXCEPTION
            'Reference object DATE_FORMAT does not exist';
    END IF;


    SELECT id
    INTO v_transaction_type_ref_id
    FROM reference_object
    WHERE LOWER(TRIM(ref_obj_name)) = 'transaction_type';

    IF v_transaction_type_ref_id IS NULL THEN
        RAISE EXCEPTION
            'Reference object TRANSACTION_TYPE does not exist';
    END IF;


    SELECT id
    INTO v_country_ref_object_id
    FROM reference_object
    WHERE LOWER(TRIM(ref_obj_name)) = 'country_code';

    IF v_country_ref_object_id IS NULL THEN
        RAISE EXCEPTION
            'Reference object COUNTRY_CODE does not exist';
    END IF;


    SELECT id
    INTO v_state_ref_object_id
    FROM reference_object
    WHERE LOWER(TRIM(ref_obj_name)) = 'state_code';

    IF v_state_ref_object_id IS NULL THEN
        RAISE EXCEPTION
            'Reference object STATE_CODE does not exist';
    END IF;


    SELECT id
    INTO v_time_zone_ref_object_id
    FROM reference_object
    WHERE LOWER(TRIM(ref_obj_name)) = 'time_zone';

    IF v_time_zone_ref_object_id IS NULL THEN
        RAISE EXCEPTION
            'Reference object TIME_ZONE does not exist';
    END IF;


    SELECT id
    INTO v_password_history_ref_id
    FROM reference_object
    WHERE LOWER(TRIM(ref_obj_name)) = 'password_history_limit';

    IF v_password_history_ref_id IS NULL THEN
        RAISE EXCEPTION
            'Reference object PASSWORD_HISTORY_LIMIT does not exist';
    END IF;


    ----------------------------------------------------------------
    -- 3. Copy canonical reference values to user's reference values
    ----------------------------------------------------------------

    INSERT INTO user_reference_value
    (user_id,
     ref_obj_name_id,
     reference_code,
     reference_code_description,
     is_active,
     reference_code_2,
     reference_code_3,
     created_by,
     is_deleted)
    SELECT p_user_id,
           rv.ref_obj_name_id,
           rv.reference_code,
           rv.reference_code_description,
           TRUE,
           rv.reference_code_2,
           rv.reference_code_3,
           p_user_id,
           FALSE
    FROM reference_values rv

    ON CONFLICT
        (
        ref_obj_name_id,
        (LOWER(TRIM(reference_code))),
        user_id
        )
    WHERE is_deleted = FALSE
        DO NOTHING;


    ----------------------------------------------------------------
    -- 4. Resolve required default user reference values
    ----------------------------------------------------------------

    -- Default currency: INR

    SELECT id
    INTO v_currency_code_id
    FROM user_reference_value
    WHERE user_id = p_user_id
      AND ref_obj_name_id = v_currency_ref_object_id
      AND LOWER(TRIM(reference_code)) = 'inr'
      AND is_active = TRUE
      AND is_deleted = FALSE;

    IF v_currency_code_id IS NULL THEN
        RAISE EXCEPTION
            'INR currency reference value is unavailable for user %',
            p_user_id;
    END IF;


    -- Default date format: DD/MM/YYYY

    SELECT id
    INTO v_date_format_id
    FROM user_reference_value
    WHERE user_id = p_user_id
      AND ref_obj_name_id = v_date_format_ref_object_id
      AND LOWER(TRIM(reference_code)) = 'dd/mm/yyyy'
      AND is_active = TRUE
      AND is_deleted = FALSE;

    IF v_date_format_id IS NULL THEN
        RAISE EXCEPTION
            'Date format dd/MM/yyyy is unavailable for user %',
            p_user_id;
    END IF;


    -- Default country: IN

    SELECT id
    INTO v_country_code_id
    FROM user_reference_value
    WHERE user_id = p_user_id
      AND ref_obj_name_id = v_country_ref_object_id
      AND LOWER(TRIM(reference_code)) = 'in'
      AND is_active = TRUE
      AND is_deleted = FALSE;

    IF v_country_code_id IS NULL THEN
        RAISE EXCEPTION
            'Country code IN is unavailable for user %',
            p_user_id;
    END IF;


    -- Default state: IN-KA

    SELECT id
    INTO v_state_code_id
    FROM user_reference_value
    WHERE user_id = p_user_id
      AND ref_obj_name_id = v_state_ref_object_id
      AND LOWER(TRIM(reference_code)) = 'in-ka'
      AND is_active = TRUE
      AND is_deleted = FALSE;

    IF v_state_code_id IS NULL THEN
        RAISE EXCEPTION
            'State code IN-KA is unavailable for user %',
            p_user_id;
    END IF;


    -- Default time zone: Asia/Kolkata

    SELECT id
    INTO v_time_zone_id
    FROM user_reference_value
    WHERE user_id = p_user_id
      AND ref_obj_name_id = v_time_zone_ref_object_id
      AND LOWER(TRIM(reference_code)) = 'asia/kolkata'
      AND is_active = TRUE
      AND is_deleted = FALSE;

    IF v_time_zone_id IS NULL THEN
        RAISE EXCEPTION
            'Time zone Asia/Kolkata is unavailable for user %',
            p_user_id;
    END IF;


    -- Default password history limit: 5

    SELECT id
    INTO v_psd_his_limit_id
    FROM user_reference_value
    WHERE user_id = p_user_id
      AND ref_obj_name_id = v_password_history_ref_id
      AND LOWER(TRIM(reference_code)) = '5'
      AND is_active = TRUE
      AND is_deleted = FALSE;

    IF v_psd_his_limit_id IS NULL THEN
        RAISE EXCEPTION
            'Password history limit 5 is unavailable for user %',
            p_user_id;
    END IF;


    ----------------------------------------------------------------
    -- 5. Associate user with themselves as admin and activate account
    ----------------------------------------------------------------

    UPDATE users
    SET admin_user_id = p_user_id,
        is_active     = TRUE,
        updated_at    = CURRENT_TIMESTAMP,
        updated_by    = 'System'
    WHERE id = p_user_id;


    ----------------------------------------------------------------
    -- 6. Insert default currency configuration
    ----------------------------------------------------------------

    INSERT INTO system_config
    (config_name,
     config_value,
     description,
     user_id,
     created_by)
    VALUES ('Currency',
            v_currency_code_id,
            'Default currency used to determine currency symbols and currency display in the application.',
            p_user_id,
            p_user_id)
    ON CONFLICT
        (
        (LOWER(TRIM(config_name))),
        user_id
        )
        DO NOTHING;


    ----------------------------------------------------------------
    -- 7. Insert default date format configuration
    ----------------------------------------------------------------

    INSERT INTO system_config
    (config_name,
     config_value,
     description,
     user_id,
     created_by)
    SELECT 'Date Format',
           urv.id,
           urv.reference_code_description,
           p_user_id,
           p_user_id
    FROM user_reference_value urv
    WHERE urv.id = v_date_format_id
      AND urv.user_id = p_user_id
      AND urv.is_deleted = FALSE

    ON CONFLICT
        (
        (LOWER(TRIM(config_name))),
        user_id
        )
        DO NOTHING;


    ----------------------------------------------------------------
    -- 8. Insert country, state, time zone and password history config
    ----------------------------------------------------------------

    INSERT INTO system_config
    (config_name,
     config_value,
     description,
     user_id,
     created_by)
    VALUES ('Country',
            v_country_code_id,
            'Country used for public holiday calendars.',
            p_user_id,
            p_user_id),
           ('State',
            v_state_code_id,
            'State used for public holiday calendars.',
            p_user_id,
            p_user_id),
           ('Time Zone',
            v_time_zone_id,
            'Time zone used for scheduled tasks.',
            p_user_id,
            p_user_id),
           ('Password History Limit',
            v_psd_his_limit_id,
            'Prevents reuse of the last specified number of passwords.',
            p_user_id,
            p_user_id)
    ON CONFLICT
        (
        (LOWER(TRIM(config_name))),
        user_id
        )
        DO NOTHING;


    ----------------------------------------------------------------
    -- 9. Associate user with themselves as admin and activate account
    ----------------------------------------------------------------

    -- This update is intentionally performed after validation and
    -- before copying the user's default categories.

    ----------------------------------------------------------------
    -- 10. Copy global categories to user categories
    --     Map global transaction type codes to user transaction type IDs
    ----------------------------------------------------------------

    INSERT INTO user_categories
    (user_id,
     transaction_type,
     category_name,
     category_description,
     is_active,
     created_by)
    SELECT p_user_id,
           user_tt.id,
           c.category_name,
           c.category_description,
           c.is_active,
           p_user_id
    FROM categories c

             JOIN reference_values global_tt
                  ON global_tt.id = c.transaction_type

             JOIN user_reference_value user_tt
                  ON user_tt.user_id = p_user_id
                      AND user_tt.ref_obj_name_id = v_transaction_type_ref_id
                      AND LOWER(TRIM(user_tt.reference_code)) =
                          LOWER(TRIM(global_tt.reference_code))
                      AND user_tt.is_active = TRUE
                      AND user_tt.is_deleted = FALSE

    ON CONFLICT
        (
        user_id,
        transaction_type,
        (LOWER(TRIM(category_name)))
        )
    WHERE is_deleted = FALSE
        DO NOTHING;


    ----------------------------------------------------------------
    -- 11. Copy global subcategories to user subcategories
    ----------------------------------------------------------------

    INSERT INTO user_subcategories
    (user_category_id,
     subcategory_name,
     subcategory_description,
     is_active,
     created_by)
    SELECT uc.id,
           sc.subcategory_name,
           sc.subcategory_description,
           sc.is_active,
           p_user_id
    FROM subcategories sc

             JOIN categories c
                  ON c.id = sc.category_id

             JOIN reference_values global_tt
                  ON global_tt.id = c.transaction_type

             JOIN user_reference_value user_tt
                  ON user_tt.user_id = p_user_id
                      AND user_tt.ref_obj_name_id = v_transaction_type_ref_id
                      AND LOWER(TRIM(user_tt.reference_code)) =
                          LOWER(TRIM(global_tt.reference_code))
                      AND user_tt.is_active = TRUE
                      AND user_tt.is_deleted = FALSE

             JOIN user_categories uc
                  ON uc.user_id = p_user_id
                      AND uc.transaction_type = user_tt.id
                      AND LOWER(TRIM(uc.category_name)) =
                          LOWER(TRIM(c.category_name))

    ON CONFLICT
        (
        user_category_id,
        (LOWER(TRIM(subcategory_name)))
        )
    WHERE is_deleted = FALSE
        DO NOTHING;


END;
$$;
