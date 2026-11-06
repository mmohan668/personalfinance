-- ============================================================
-- Financial Transactions
-- ============================================================

CREATE TABLE financial_transactions
(
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    amount         NUMERIC(19, 4) NOT NULL
        CHECK (amount <> 0),

    transaction_at TIMESTAMPTZ    NOT NULL,

    subcategory_id BIGINT         NOT NULL,
    location_id    BIGINT,

    remarks        VARCHAR(4000)
        CHECK (remarks IS NULL OR TRIM(remarks) <> ''),

    admin_user_id  BIGINT         NOT NULL,
    created_by     BIGINT         NOT NULL,
    created_at     TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by     BIGINT,
    updated_at     TIMESTAMPTZ,

    CONSTRAINT fk_financial_transactions_subcategory
        FOREIGN KEY (subcategory_id)
            REFERENCES user_subcategories (id),

    CONSTRAINT fk_financial_transactions_location
        FOREIGN KEY (location_id, admin_user_id)
            REFERENCES user_reference_value (id, user_id),

    CONSTRAINT fk_financial_transactions_admin_user
        FOREIGN KEY (admin_user_id)
            REFERENCES users (id),

    CONSTRAINT fk_financial_transactions_created_by
        FOREIGN KEY (created_by)
            REFERENCES users (id),

    CONSTRAINT fk_financial_transactions_updated_by
        FOREIGN KEY (updated_by)
            REFERENCES users (id),

    CONSTRAINT chk_financial_transactions_updated_status
        CHECK (
            (updated_by IS NULL AND updated_at IS NULL)
                OR
            (updated_by IS NOT NULL AND updated_at IS NOT NULL)
            )
);


-- ============================================================
-- Indexes
-- ============================================================

CREATE INDEX idx_financial_transactions_admin_date
    ON financial_transactions (admin_user_id, transaction_at DESC);

CREATE INDEX idx_financial_transactions_admin_subcategory
    ON financial_transactions (admin_user_id, subcategory_id);

CREATE INDEX idx_financial_transactions_admin_location
    ON financial_transactions (admin_user_id, location_id);

CREATE INDEX idx_financial_transactions_admin_created_by
    ON financial_transactions (admin_user_id, created_by);

CREATE INDEX idx_financial_transactions_admin_created_at
    ON financial_transactions (admin_user_id, created_at DESC);

CREATE INDEX idx_financial_transactions_admin_updated_at
    ON financial_transactions (admin_user_id, updated_at DESC);

-- Supports FK checks and lookups by referenced subcategory/location.
CREATE INDEX idx_financial_transactions_subcategory_id
    ON financial_transactions (subcategory_id);

CREATE INDEX idx_financial_transactions_location_owner
    ON financial_transactions (location_id, admin_user_id) WHERE location_id IS NOT NULL;
