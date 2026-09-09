CREATE TABLE reference_object
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ref_obj_name VARCHAR(100) NOT NULL
        CHECK ( TRIM(ref_obj_name) <> '' )
);

CREATE UNIQUE INDEX ux_reference_object_name
    ON reference_object (LOWER(TRIM(ref_obj_name)));

INSERT INTO reference_object (ref_obj_name)
VALUES ('EXPENSE'),
       ('INCOME'),
       ('TRANSFER'),
       ('INVESTMENT');