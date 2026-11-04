CREATE TABLE profile_pictures
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    image_path   VARCHAR(500) NOT NULL
        CHECK (TRIM(image_path) <> ''),
    content_type VARCHAR(100),
    file_size    BIGINT
        CHECK (file_size IS NULL OR file_size > 0),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_profile_pictures_user
        FOREIGN KEY (user_id) REFERENCES users (id),

    CONSTRAINT uq_profile_pictures_user
        UNIQUE (user_id)
);