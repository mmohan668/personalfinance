DO
$$
    DECLARE
        v_system_user_count      INTEGER;
        v_transaction_type_count INTEGER;
        v_currency_code_count    INTEGER;
        v_date_format_count      INTEGER;
        v_country_code_count     INTEGER;
        v_state_code_count       INTEGER;
        v_time_zone_count        INTEGER;
        v_pwd_his_limit_count    INTEGER;
        v_pwd_exp_days_count     INTEGER;
        v_enable_reports_count   INTEGER;
        v_report_frequency_count INTEGER;
    BEGIN
        SELECT COUNT(*)
        INTO v_system_user_count
        FROM users
        WHERE LOWER(TRIM(username)) = 'system'
          AND is_deleted = FALSE;

        IF
            v_system_user_count <> 1 THEN
            RAISE EXCEPTION
                'Expected exactly one active System user, but found %.',
                v_system_user_count;
        END IF;

        SELECT COUNT(*)
        INTO v_transaction_type_count
        FROM reference_object
        WHERE LOWER(TRIM(ref_obj_name)) = 'transaction_type';

        IF
            v_transaction_type_count <> 1 THEN
            RAISE EXCEPTION
                'Expected exactly one TRANSACTION_TYPE reference object, but found %.',
                v_transaction_type_count;
        END IF;

        SELECT COUNT(*)
        INTO v_currency_code_count
        FROM reference_object
        WHERE LOWER(TRIM(ref_obj_name)) = 'currency_code';

        IF
            v_currency_code_count <> 1 THEN
            RAISE EXCEPTION
                'Expected exactly one CURRENCY_CODE reference object, but found %.',
                v_currency_code_count;
        END IF;

        SELECT COUNT(*)
        INTO v_date_format_count
        FROM reference_object
        WHERE LOWER(TRIM(ref_obj_name)) = 'date_format';

        IF
            v_date_format_count <> 1 THEN
            RAISE EXCEPTION
                'Expected exactly one DATE_FORMAT reference object, but found %.',
                v_date_format_count;
        END IF;

        SELECT COUNT(*)
        INTO v_country_code_count
        FROM reference_object
        WHERE LOWER(TRIM(ref_obj_name)) = 'country_code';

        IF v_country_code_count <> 1 THEN
            RAISE EXCEPTION
                'Expected exactly one COUNTRY_CODE reference object, but found %.',
                v_country_code_count;
        END IF;

        SELECT COUNT(*)
        INTO v_state_code_count
        FROM reference_object
        WHERE LOWER(TRIM(ref_obj_name)) = 'state_code';

        IF v_state_code_count <> 1 THEN
            RAISE EXCEPTION
                'Expected exactly one STATE_CODE reference object, but found %.',
                v_state_code_count;
        END IF;

        SELECT COUNT(*)
        INTO v_time_zone_count
        FROM reference_object
        WHERE LOWER(TRIM(ref_obj_name)) = 'time_zone';

        IF v_time_zone_count <> 1 THEN
            RAISE EXCEPTION
                'Expected exactly one TIME_ZONE reference object, but found %.',
                v_time_zone_count;
        END IF;

        SELECT COUNT(*)
        INTO v_pwd_his_limit_count
        FROM reference_object
        WHERE LOWER(TRIM(ref_obj_name)) = 'password_history_limit';

        IF v_time_zone_count <> 1 THEN
            RAISE EXCEPTION
                'Expected exactly one PASSWORD_HISTORY_LIMIT reference object, but found %.',
                v_time_zone_count;
        END IF;

        SELECT COUNT(*)
        INTO v_pwd_exp_days_count
        FROM reference_object
        WHERE LOWER(TRIM(ref_obj_name)) = 'password_expiry_days';

        IF v_time_zone_count <> 1 THEN
            RAISE EXCEPTION
                'Expected exactly one PASSWORD_EXPIRY_DAYS reference object, but found %.',
                v_time_zone_count;
        END IF;

        SELECT COUNT(*) INTO v_enable_reports_count FROM reference_object
        WHERE LOWER(TRIM(ref_obj_name)) = 'enable_reports';
        IF v_enable_reports_count <> 1 THEN
            RAISE EXCEPTION 'Expected exactly one ENABLE_REPORTS reference object, but found %.', v_enable_reports_count;
        END IF;
        SELECT COUNT(*) INTO v_report_frequency_count FROM reference_object
        WHERE LOWER(TRIM(ref_obj_name)) = 'report_frequency';
        IF v_report_frequency_count <> 1 THEN
            RAISE EXCEPTION 'Expected exactly one REPORT_FREQUENCY reference object, but found %.', v_report_frequency_count;
        END IF;
    END;
$$;

WITH system_account AS
         (SELECT id
          FROM users
          WHERE LOWER(TRIM(username)) = 'system'
            AND is_deleted = FALSE),

     reference_objects AS
         (SELECT id,
                 LOWER(TRIM(ref_obj_name)) AS ref_obj_name
          FROM reference_object
          WHERE LOWER(TRIM(ref_obj_name)) IN
                ('transaction_type',
                 'currency_code',
                 'date_format',
                 'country_code',
                 'state_code',
                 'time_zone',
                 'password_history_limit',
                 'password_expiry_days',
                 'enable_reports',
                 'report_frequency')),

     initial_reference_values
         (ref_obj_name,
          reference_code_description,
          reference_code,
          reference_code_2,
          reference_code_3) AS
         (VALUES
              -- Transaction types
              ('transaction_type', 'Money Spent', 'EXPENSE', NULL, NULL),
              ('transaction_type', 'Money Received', 'INCOME', NULL, NULL),
              ('transaction_type', 'Money Invested', 'INVESTMENT', NULL, NULL),
              ('transaction_type', 'Money Transferred', 'TRANSFER', NULL, NULL),

              -- Currency codes
              ('currency_code', 'Indian Rupee', 'INR', 'en-IN', NULL),
              ('currency_code', 'US Dollar', 'USD', 'en-US', NULL),

              -- Date formats
              ('date_format', 'Day/Month/Year', 'dd/MM/yyyy', NULL, NULL),
              ('date_format', 'Month/Day/Year', 'MM/dd/yyyy', NULL, NULL),

              -- Country codes
              ('country_code', 'India', 'IN', NULL, NULL),
              ('country_code', 'United States', 'US', NULL, NULL),

              -- State/subdivision codes (code 2 stores the country code)
              ('state_code', 'Karnataka', 'IN-KA', 'IN', NULL),
              ('state_code', 'California', 'US-CA', 'US', NULL),

              -- IANA time zones
              ('time_zone', 'India Standard Time', 'Asia/Kolkata', NULL, NULL),
              ('time_zone', 'Pacific Time (United States)', 'America/Los_Angeles', 'US-CA', NULL),

              -- Password history limit
              ('password_history_limit', 'Prevents reuse of the last 5 passwords', '5', NULL, NULL),

              -- Password Expiry Days
              ('password_expiry_days', 'Specifies the number of days after which a password expires', '30', NULL, NULL),

              -- Report preferences
              ('enable_reports', 'Do not send financial transaction reports to the user''s email.', 'No', NULL, NULL),
              ('enable_reports', 'Send financial transaction reports to the user''s email at the configured frequency.', 'Yes', NULL, NULL),
              ('report_frequency', 'Send a financial transaction report once each day for the previous day.', 'Daily', NULL, NULL),
              ('report_frequency', 'Send a financial transaction report each Monday for the previous Sunday through Saturday.', 'Weekly', NULL, NULL),
              ('report_frequency', 'Send a financial transaction report on the first day of each quarter for the previous quarter.', 'Quarterly', NULL, NULL),
              ('report_frequency', 'Send a financial transaction report on January 1 and July 1 for the previous six months.', 'HalfYearly', NULL, NULL),
              ('report_frequency', 'Send a financial transaction report once each month.', 'Monthly', NULL, NULL),
              ('report_frequency', 'Send a financial transaction report on January 1 for the previous year.', 'Yearly', NULL, NULL))

INSERT
INTO reference_values
(ref_obj_name_id,
 reference_code_description,
 reference_code,
 reference_code_2,
 reference_code_3,
 created_by)
SELECT reference_objects.id,
       initial_reference_values.reference_code_description,
       initial_reference_values.reference_code,
       initial_reference_values.reference_code_2,
       initial_reference_values.reference_code_3,
       system_account.id
FROM initial_reference_values
         INNER JOIN reference_objects
                    ON reference_objects.ref_obj_name =
                       initial_reference_values.ref_obj_name
         CROSS JOIN system_account
ON CONFLICT DO NOTHING;
