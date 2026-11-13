CREATE
    OR REPLACE PROCEDURE initialize_new_user(
    IN p_user_id BIGINT
)
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_currency_ref_object_id    BIGINT;
    v_currency_code_id          BIGINT;
    v_date_format_ref_object_id BIGINT;
    v_date_format_id            BIGINT;
    v_transaction_type_ref_id   BIGINT;
    v_country_code_id           BIGINT;
    v_state_code_id             BIGINT;
    v_time_zone_id              BIGINT;
    v_psd_his_limit_id          BIGINT;
BEGIN

    -- 1. Validate user
    IF
        p_user_id IS NULL THEN
        RAISE EXCEPTION 'User ID is required';
    END IF;

    IF
        NOT EXISTS (SELECT 1
                    FROM users
                    WHERE id = p_user_id
                      AND is_deleted = FALSE) THEN
        RAISE EXCEPTION 'User with id % does not exist or is deleted', p_user_id;
    END IF;

    -- 2. Associate user with themselves as admin and activate account
    UPDATE users
    SET admin_user_id = p_user_id,
        is_active     = TRUE,
        updated_at    = CURRENT_TIMESTAMP,
        updated_by    = 'System'
    WHERE id = p_user_id;

    -- 3. Copy canonical reference values to the user's reference values.
    INSERT INTO user_reference_value
    (user_id, ref_obj_name_id, reference_code,
     reference_code_description, is_active,
     reference_code_2, reference_code_3, created_by)
    SELECT p_user_id,
           rv.ref_obj_name_id,
           rv.reference_code,
           rv.reference_code_description,
           TRUE,
           rv.reference_code_2,
           rv.reference_code_3,
           p_user_id
    FROM reference_values rv
    ON CONFLICT ( ref_obj_name_id, (LOWER(TRIM(reference_code))), user_id ) DO NOTHING;

    -- 4. Resolve required reference object IDs
    SELECT id
    INTO v_currency_ref_object_id
    FROM reference_object
    WHERE LOWER(TRIM(ref_obj_name)) = 'currency_code';

    SELECT id
    INTO v_date_format_ref_object_id
    FROM reference_object
    WHERE LOWER(TRIM(ref_obj_name)) = 'date_format';

    SELECT id
    INTO v_transaction_type_ref_id
    FROM reference_object
    WHERE LOWER(TRIM(ref_obj_name)) = 'transaction_type';

    IF
        v_currency_ref_object_id IS NULL THEN
        RAISE EXCEPTION 'Reference object CURRENCY_CODE does not exist';
    END IF;
    IF
        v_date_format_ref_object_id IS NULL THEN
        RAISE EXCEPTION 'Reference object DATE_FORMAT does not exist';
    END IF;
    IF
        v_transaction_type_ref_id IS NULL THEN
        RAISE EXCEPTION 'Reference object TRANSACTION_TYPE does not exist';
    END IF;

    -- 5. Resolve user's INR reference value
    SELECT id
    INTO v_currency_code_id
    FROM user_reference_value
    WHERE user_id = p_user_id
      AND ref_obj_name_id = v_currency_ref_object_id
      AND LOWER(TRIM(reference_code)) = 'inr'
      AND is_active = TRUE;

    IF
        v_currency_code_id IS NULL THEN
        RAISE EXCEPTION 'INR currency reference value is unavailable for user %', p_user_id;
    END IF;

    -- 6. Set default currency without overwriting existing preference
    INSERT INTO system_config
        (config_name, config_value, description, user_id, created_by)
    VALUES ('Currency',
            v_currency_code_id,
            'Default currency used to determine currency symbols and currency display in the application.',
            p_user_id,
            p_user_id)
    ON CONFLICT ((LOWER(TRIM(config_name))), user_id) DO NOTHING;

    -- 7. Resolve user's default date format
    SELECT id
    INTO v_date_format_id
    FROM user_reference_value
    WHERE user_id = p_user_id
      AND ref_obj_name_id = v_date_format_ref_object_id
      AND LOWER(TRIM(reference_code)) = 'dd/mm/yyyy'
      AND is_active = TRUE;

    IF
        v_date_format_id IS NULL THEN
        RAISE EXCEPTION 'Date format dd/MM/yyyy is unavailable for user %', p_user_id;
    END IF;

    -- 8. Set default date format using the user's own reference value
    INSERT INTO system_config
        (config_name, config_value, description, user_id, created_by)
    SELECT 'Date Format',
           urv.id,
           urv.reference_code_description,
           p_user_id,
           p_user_id
    FROM user_reference_value urv
    WHERE urv.id = v_date_format_id
      AND urv.user_id = p_user_id
    ON CONFLICT ((LOWER(TRIM(config_name))), user_id) DO NOTHING;

    -- 9. Resolve default country, state, and time zone
    SELECT id
    INTO v_country_code_id
    FROM user_reference_value
    WHERE user_id = p_user_id
      AND LOWER(TRIM(reference_code)) = 'in'
      AND ref_obj_name_id = (SELECT id
                             FROM reference_object
                             WHERE LOWER(TRIM(ref_obj_name)) = 'country_code')
      AND is_active = TRUE;

    IF v_country_code_id IS NULL THEN
        RAISE EXCEPTION 'Country code IN is unavailable for user %', p_user_id;
    END IF;

    SELECT id
    INTO v_state_code_id
    FROM user_reference_value
    WHERE user_id = p_user_id
      AND LOWER(TRIM(reference_code)) = 'in-ka'
      AND ref_obj_name_id = (SELECT id
                             FROM reference_object
                             WHERE LOWER(TRIM(ref_obj_name)) = 'state_code')
      AND is_active = TRUE;

    IF v_state_code_id IS NULL THEN
        RAISE EXCEPTION 'State code IN-KA is unavailable for user %', p_user_id;
    END IF;

    SELECT id
    INTO v_time_zone_id
    FROM user_reference_value
    WHERE user_id = p_user_id
      AND LOWER(TRIM(reference_code)) = 'asia/kolkata'
      AND ref_obj_name_id = (SELECT id
                             FROM reference_object
                             WHERE LOWER(TRIM(ref_obj_name)) = 'time_zone')
      AND is_active = TRUE;

    IF v_time_zone_id IS NULL THEN
        RAISE EXCEPTION 'Time zone Asia/Kolkata is unavailable for user %', p_user_id;
    END IF;

    SELECT id
    INTO v_psd_his_limit_id
    FROM user_reference_value
    WHERE user_id = p_user_id
      AND LOWER(TRIM(reference_code)) = '5'
      AND ref_obj_name_id = (SELECT id
                             FROM reference_object
                             WHERE LOWER(TRIM(ref_obj_name)) = 'password_history_limit')
      AND is_active = TRUE;

    IF v_psd_his_limit_id IS NULL THEN
        RAISE EXCEPTION 'Password history limit 5 is unavailable for user %', p_user_id;
    END IF;

    -- 10. Set default country, state, and time zone
    INSERT INTO system_config
        (config_name, config_value, description, user_id, created_by)
    VALUES ('Country', v_country_code_id, 'Country used for public holiday calendars.', p_user_id, p_user_id),
           ('State', v_state_code_id, 'State used for public holiday calendars.', p_user_id, p_user_id),
           ('Time Zone', v_time_zone_id, 'Time zone used for scheduled tasks.', p_user_id, p_user_id),
           ('Password History Limit', v_psd_his_limit_id, 'Prevents reuse of the last specified number of passwords.',
            p_user_id, p_user_id)
    ON CONFLICT ((LOWER(TRIM(config_name))), user_id) DO NOTHING;

    -- 11. Copy global categories, mapping global transaction type codes to this user's transaction type IDs.
    INSERT INTO user_categories
    (user_id, transaction_type, category_name,
     category_description, is_active, created_by)
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
                      AND LOWER(TRIM(user_tt.reference_code)) = LOWER(TRIM(global_tt.reference_code))
                      AND user_tt.is_active = TRUE
    ON CONFLICT ( user_id, transaction_type, (LOWER(TRIM(category_name))) ) DO NOTHING;

    -- 12. Copy global subcategories to the mapped user categories.
    INSERT INTO user_subcategories
    (user_category_id, subcategory_name,
     subcategory_description, is_active, created_by)
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
                      AND LOWER(TRIM(user_tt.reference_code)) = LOWER(TRIM(global_tt.reference_code))
                      AND user_tt.is_active = TRUE
             JOIN user_categories uc
                  ON uc.user_id = p_user_id
                      AND uc.transaction_type = user_tt.id
                      AND LOWER(TRIM(uc.category_name)) = LOWER(TRIM(c.category_name))
    ON CONFLICT ( user_category_id, (LOWER(TRIM(subcategory_name))) ) DO NOTHING;
END;
$$;
