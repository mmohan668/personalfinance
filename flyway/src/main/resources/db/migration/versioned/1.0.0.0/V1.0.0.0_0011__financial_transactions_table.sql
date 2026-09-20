CREATE TABLE financial_transactions
(
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    amount              NUMERIC(19, 4)                            NOT NULL,
    transaction_at      TIMESTAMP                                 NOT NULL,
    transaction_type_id BIGINT REFERENCES reference_value (id)    NOT NULL,
    category_id         BIGINT REFERENCES user_categories (id)    NOT NULL,
    subcategory_id      BIGINT REFERENCES user_subcategories (id) NOT NULL,
    location_id         BIGINT REFERENCES reference_value (id),
    remarks             VARCHAR(4000),
    admin_user_id       BIGINT REFERENCES users (id)              NOT NULL,
    created_by          BIGINT REFERENCES users (id)              NOT NULL,
    created_at          TIMESTAMPTZ                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          BIGINT REFERENCES users (id),
    updated_at          TIMESTAMPTZ
);

CREATE INDEX idx_financial_transactions_admin_date
    ON financial_transactions (admin_user_id, transaction_at);

CREATE INDEX idx_financial_transactions_admin_type
    ON financial_transactions (admin_user_id, transaction_type_id);

CREATE INDEX idx_financial_transactions_admin_category
    ON financial_transactions (admin_user_id, category_id);

CREATE INDEX idx_financial_transactions_admin_subcategory
    ON financial_transactions (admin_user_id, subcategory_id);

CREATE INDEX idx_financial_transactions_admin_location
    ON financial_transactions (admin_user_id, location_id);

CREATE INDEX idx_financial_transactions_admin_created_by
    ON financial_transactions (admin_user_id, created_by);

CREATE INDEX idx_financial_transactions_admin_created_at
    ON financial_transactions (admin_user_id, created_at);

CREATE INDEX idx_financial_transactions_admin_updated_by
    ON financial_transactions (admin_user_id, updated_by);

CREATE INDEX idx_financial_transactions_admin_updated_at
    ON financial_transactions (admin_user_id, updated_at);