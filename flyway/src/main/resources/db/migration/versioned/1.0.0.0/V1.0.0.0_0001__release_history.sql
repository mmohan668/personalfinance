-- V1.0.0.0_0001_release_history.sql

CREATE TABLE release_history
(
    id              SERIAL PRIMARY KEY,
    release_version VARCHAR(50) NOT NULL, -- e.g., 1.0.0.0
    release_date    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    description     TEXT,
    applied_by      VARCHAR(100)         DEFAULT CURRENT_USER,
    applied_on      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- First insert for release 1.0.0.0
INSERT INTO release_history (release_version, description)
VALUES ('1.0.0.0', 'Initial baseline release with schema setup');
