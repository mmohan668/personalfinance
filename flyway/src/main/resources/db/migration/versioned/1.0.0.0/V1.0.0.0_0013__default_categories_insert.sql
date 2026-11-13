DO
$$
    DECLARE
        v_system_user_count      INTEGER;
        v_transaction_type_count INTEGER;
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
    END;
$$;

WITH system_account AS
         (SELECT id
          FROM users
          WHERE LOWER(TRIM(username)) = 'system'
            AND is_deleted = FALSE),
     transaction_type_object AS
         (SELECT id
          FROM reference_object
          WHERE LOWER(TRIM(ref_obj_name)) = 'transaction_type'),
     initial_categories
         (
          transaction_type,
          category_name,
          category_description,
          is_active
             ) AS
         (VALUES

              -- EXPENSE
              ('EXPENSE',
               'Banking & Finance',
               'Banking fees, financial charges, interest and penalties',
               TRUE),
              ('EXPENSE',
               'Bills & Utilities',
               'Recurring household utility and communication bills',
               TRUE),
              ('EXPENSE',
               'Child Expenses',
               'Expenses related to children and their daily needs',
               TRUE),
              ('EXPENSE',
               'Dining & Food Delivery',
               'Restaurant meals, cafes, fast food and food delivery',
               TRUE),
              ('EXPENSE',
               'Donations & Contributions',
               'Charitable donations, religious giving, tips and contributions',
               TRUE),
              ('EXPENSE',
               'Education',
               'Education fees, learning materials, courses and coaching',
               TRUE),
              ('EXPENSE',
               'Electronics',
               'Electronic devices, accessories, repairs and related expenses',
               TRUE),
              ('EXPENSE',
               'Entertainment',
               'Entertainment, hobbies, gaming, subscriptions and events',
               TRUE),
              ('EXPENSE',
               'Food & Groceries',
               'Groceries, fresh food, dairy, meat and household food items',
               TRUE),
              ('EXPENSE',
               'Healthcare',
               'Medical consultations, medicines, tests and health insurance',
               TRUE),
              ('EXPENSE',
               'Housing & Household',
               'Home rent, loan payments, maintenance and household expenses',
               TRUE),
              ('EXPENSE',
               'Insurance',
               'Life insurance and other insurance-related expenses',
               TRUE),
              ('EXPENSE',
               'Lifestyle Shopping',
               'Clothing, footwear, accessories, jewelry and personal shopping',
               TRUE),
              ('EXPENSE',
               'Personal Care',
               'Personal grooming, cosmetics, fitness and laundry expenses',
               TRUE),
              ('EXPENSE',
               'Transportation',
               'Fuel, charging, public transport, parking and vehicle expenses',
               TRUE),
              ('EXPENSE',
               'Travel & Vacation',
               'Travel accommodation, sightseeing, documents and insurance',
               TRUE),

              -- INCOME
              ('INCOME',
               'Business Income',
               'Income received from business activities',
               TRUE),
              ('INCOME',
               'Freelance & Contract Work',
               'Income received from freelance and contract work',
               TRUE),
              ('INCOME',
               'Gifts & Windfalls',
               'Cash gifts, prize money and other unexpected receipts',
               TRUE),
              ('INCOME',
               'Government Benefits',
               'Pensions, tax refunds and other government benefits',
               TRUE),
              ('INCOME',
               'Investment Income',
               'Income generated from investments and financial assets',
               TRUE),
              ('INCOME',
               'Other Income',
               'Income that does not fit into another income category',
               TRUE),
              ('INCOME',
               'Rental Income',
               'Income received from rental properties',
               TRUE),
              ('INCOME',
               'Salary & Employment',
               'Salary, bonuses and other employment-related income',
               TRUE),

              -- TRANSFER
              ('TRANSFER',
               'Transfer',
               'Movement of money between people or accounts',
               TRUE),

              -- INVESTMENT
              ('INVESTMENT',
               'Investments',
               'Money allocated to investments and long-term financial assets',
               TRUE))
INSERT
INTO categories
(transaction_type,
 category_name,
 category_description,
 is_active,
 created_by)
SELECT rv.id,
       ic.category_name,
       ic.category_description,
       ic.is_active,
       su.id
FROM initial_categories ic
         INNER JOIN transaction_type_object rto
                    ON TRUE
         INNER JOIN reference_values rv
                    ON rv.ref_obj_name_id = rto.id
                        AND LOWER(TRIM(rv.reference_code)) =
                            LOWER(TRIM(ic.transaction_type))
         CROSS JOIN system_account su
ON CONFLICT ( transaction_type, (LOWER(TRIM(category_name))) ) DO NOTHING;