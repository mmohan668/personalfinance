CREATE TABLE reference_object
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ref_obj_name VARCHAR(100) NOT NULL
        CHECK ( TRIM(ref_obj_name) <> '' ),
    created_by   VARCHAR(100) NOT NULL
        CHECK ( TRIM(created_by) <> '' ),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by   VARCHAR(100)
        CHECK ( TRIM(updated_by) <> '' ),
    updated_at   TIMESTAMPTZ
);

CREATE TABLE reference_value
(
    id                         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ref_obj_name_id            BIGINT       NOT NULL,
    reference_code             VARCHAR(100) NOT NULL
        CHECK ( TRIM(reference_code) <> '' ),
    reference_code_description VARCHAR(200) NOT NULL
        CHECK ( TRIM(reference_code_description) <> '' ),
    reference_code_2           VARCHAR(100),
    reference_code_3           VARCHAR(100),
    created_by                 VARCHAR(100) NOT NULL
        CHECK ( TRIM(created_by) <> '' ),
    created_at                 TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by                 VARCHAR(100)
        CHECK ( TRIM(updated_by) <> '' ),
    updated_at                 TIMESTAMPTZ
);

CREATE UNIQUE INDEX ux_reference_id_value
    ON reference_value (ref_obj_name_id, LOWER(TRIM(reference_code)));

CREATE UNIQUE INDEX ux_reference_object_name
    ON reference_object (LOWER(TRIM(ref_obj_name)));

INSERT INTO reference_object (ref_obj_name, created_by)
VALUES ('CATEGORY_TYPE', 'SYSTEM'),
       ('CURRENCY_CODE', 'SYSTEM'),
       ('LOCATION', 'SYSTEM');

INSERT INTO reference_value (ref_obj_name_id, reference_code, reference_code_2, reference_code_3,
                             reference_code_description, created_by)
VALUES ((SELECT id FROM reference_object WHERE ref_obj_name = 'CATEGORY_TYPE'),
        'EXPENSE', null, null, 'EXPENSE - Category Type', 'SYSTEM'),
       ((SELECT id FROM reference_object WHERE ref_obj_name = 'CATEGORY_TYPE'),
        'INCOME', null, null, 'INCOME - Category Type', 'SYSTEM'),
       ((SELECT id FROM reference_object WHERE ref_obj_name = 'CATEGORY_TYPE'),
        'INVESTMENT', null, null, 'INVESTMENT - Category Type', 'SYSTEM'),
       ((SELECT id FROM reference_object WHERE ref_obj_name = 'CATEGORY_TYPE'),
        'TRANSFER', null, null, 'TRANSFER - Category Type', 'SYSTEM'),
       ((SELECT id FROM reference_object WHERE ref_obj_name = 'CURRENCY_CODE'),
        'INR', 'en-IN', 'DD/MM/YYYY', 'Indian Currency Code', 'SYSTEM'),
       ((SELECT id FROM reference_object WHERE ref_obj_name = 'CURRENCY_CODE'),
        'USD', 'en-US', 'MM/DD/YYYY', 'USA Currency Code', 'SYSTEM');