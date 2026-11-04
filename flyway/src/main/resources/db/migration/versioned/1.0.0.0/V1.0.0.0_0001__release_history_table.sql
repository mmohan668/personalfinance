-- =========================================================
-- Create release history table
-- =========================================================

CREATE TABLE release_history
(
    release_history_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    release_version    VARCHAR(50) NOT NULL
        CHECK (TRIM(release_version) <> ''),

    release_date       TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    description        TEXT,

    CONSTRAINT uq_release_version
        UNIQUE (release_version)
);