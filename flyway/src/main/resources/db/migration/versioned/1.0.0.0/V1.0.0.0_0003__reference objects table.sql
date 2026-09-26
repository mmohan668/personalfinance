CREATE TABLE reference_object
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ref_obj_name VARCHAR(100) NOT NULL CHECK ( TRIM(ref_obj_name) <> '' ),
    created_by   BIGINT       NOT NULL REFERENCES users (id),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by   BIGINT REFERENCES users (id),
    updated_at   TIMESTAMPTZ
);

CREATE TABLE reference_value
(
    id                         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ref_obj_name_id            BIGINT       NOT NULL REFERENCES reference_object (id),
    reference_code             VARCHAR(100) NOT NULL CHECK ( TRIM(reference_code) <> '' ),
    reference_code_description VARCHAR(200) NOT NULL CHECK ( TRIM(reference_code_description) <> '' ),
    reference_code_2           VARCHAR(100),
    reference_code_3           VARCHAR(100),
    created_by                 BIGINT       NOT NULL REFERENCES users (id),
    created_at                 TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by                 BIGINT REFERENCES users (id),
    updated_at                 TIMESTAMPTZ
);

CREATE UNIQUE INDEX ux_reference_id_value
    ON reference_value (ref_obj_name_id, LOWER(TRIM(reference_code)));

CREATE UNIQUE INDEX ux_reference_object_name
    ON reference_object (LOWER(TRIM(ref_obj_name)));

INSERT INTO reference_object (ref_obj_name, created_by)
VALUES ('TRANSACTION_TYPE', (SELECT id FROM users WHERE username = 'PERSONALFINANCEAPP')),
       ('CURRENCY_CODE', (SELECT id FROM users WHERE username = 'PERSONALFINANCEAPP')),
       ('LOCATION', (SELECT id FROM users WHERE username = 'PERSONALFINANCEAPP'));

INSERT INTO reference_value (ref_obj_name_id, reference_code, reference_code_2, reference_code_3,
                             reference_code_description, created_by)
VALUES ((SELECT id FROM reference_object WHERE ref_obj_name = 'TRANSACTION_TYPE'),
        'EXPENSE', null, null, 'EXPENSE - Transaction Type', (SELECT id FROM users WHERE username = 'PERSONALFINANCEAPP')),
       ((SELECT id FROM reference_object WHERE ref_obj_name = 'TRANSACTION_TYPE'),
        'INCOME', null, null, 'INCOME - Transaction Type', (SELECT id FROM users WHERE username = 'PERSONALFINANCEAPP')),
       ((SELECT id FROM reference_object WHERE ref_obj_name = 'TRANSACTION_TYPE'),
        'INVESTMENT', null, null, 'INVESTMENT - Transaction Type',
        (SELECT id FROM users WHERE username = 'PERSONALFINANCEAPP')),
       ((SELECT id FROM reference_object WHERE ref_obj_name = 'TRANSACTION_TYPE'),
        'TRANSFER', null, null, 'TRANSFER - Transaction Type',
        (SELECT id FROM users WHERE username = 'PERSONALFINANCEAPP')),
       ((SELECT id FROM reference_object WHERE ref_obj_name = 'CURRENCY_CODE'),
        'INR', 'en-IN', 'DD/MM/YYYY', 'Indian Currency Code',
        (SELECT id FROM users WHERE username = 'PERSONALFINANCEAPP')),
       ((SELECT id FROM reference_object WHERE ref_obj_name = 'CURRENCY_CODE'),
        'USD', 'en-US', 'MM/DD/YYYY', 'USA Currency Code',
        (SELECT id FROM users WHERE username = 'PERSONALFINANCEAPP'));