CREATE TABLE release_history
(
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    release_version VARCHAR(50) NOT NULL
        CHECK ( TRIM(release_version) <> '' ), -- e.g., 1.0.0.0
    release_date    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    description     TEXT,

    CONSTRAINT uq_release_version UNIQUE (release_version)
);

-- First insert for release 1.0.0.0
INSERT INTO release_history (release_version, description)
VALUES ('1.0.0.0', 'Initial baseline release with schema setup');
