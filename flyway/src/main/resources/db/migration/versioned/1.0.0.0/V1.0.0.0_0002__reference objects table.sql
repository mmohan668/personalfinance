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

CREATE UNIQUE INDEX ux_reference_object_name
    ON reference_object (LOWER(TRIM(ref_obj_name)));

INSERT INTO reference_object (ref_obj_name, created_by)
VALUES ('EXPENSE', 'SYSTEM'),
       ('INCOME', 'SYSTEM'),
       ('TRANSFER', 'SYSTEM'),
       ('INVESTMENT', 'SYSTEM');