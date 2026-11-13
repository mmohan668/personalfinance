CREATE TABLE recurring_transaction_rule
(
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    admin_user_id   BIGINT         NOT NULL,
    rule_name       VARCHAR(150)   NOT NULL CHECK (TRIM(rule_name) <> ''),
    subcategory_id  BIGINT         NOT NULL,
    location_id     BIGINT,
    amount          NUMERIC(19, 4) NOT NULL CHECK (amount <> 0),
    remarks         VARCHAR(4000) CHECK (remarks IS NULL OR TRIM(remarks) <> ''),
    frequency       VARCHAR(20)    NOT NULL CHECK ( frequency IN
                                                    ('MINUTELY', 'HOURLY', 'DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY') ),
    interval_value  INTEGER        NOT NULL DEFAULT 1 CHECK (interval_value > 0),
    day_of_week     SMALLINT CHECK (day_of_week BETWEEN 1 AND 7),
    day_of_month    SMALLINT CHECK (day_of_month BETWEEN 1 AND 31),
    month_of_year   SMALLINT CHECK (month_of_year BETWEEN 1 AND 12),
    monthly_pattern VARCHAR(20) CHECK (monthly_pattern IN ('DAY_OF_MONTH', 'LAST_DAY', 'LAST_WORKING_DAY')),
    start_date      DATE           NOT NULL,
    end_date        DATE,
    resume_after_at TIMESTAMP,
    execution_time  TIME           NOT NULL,
    is_active       BOOLEAN        NOT NULL DEFAULT TRUE,
    created_by      BIGINT         NOT NULL,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT,
    updated_at      TIMESTAMPTZ,
    CONSTRAINT fk_recurring_rule_admin FOREIGN KEY (admin_user_id) REFERENCES users (id),
    CONSTRAINT fk_recurring_rule_subcategory FOREIGN KEY (subcategory_id) REFERENCES user_subcategories (id),
    CONSTRAINT fk_recurring_rule_location FOREIGN KEY (location_id, admin_user_id)
        REFERENCES user_reference_value (id, user_id),
    CONSTRAINT fk_recurring_rule_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_recurring_rule_updated_by FOREIGN KEY (updated_by) REFERENCES users (id),
    CONSTRAINT chk_recurring_rule_end_date CHECK (end_date IS NULL OR end_date >= start_date),
    CONSTRAINT chk_recurring_rule_weekly CHECK (frequency <> 'WEEKLY' OR day_of_week IS NOT NULL),
    CONSTRAINT chk_recurring_rule_monthly CHECK ( frequency <> 'MONTHLY' OR monthly_pattern IS NOT NULL ),
    CONSTRAINT chk_recurring_rule_day_of_month CHECK ( monthly_pattern <> 'DAY_OF_MONTH' OR day_of_month IS NOT NULL ),
    CONSTRAINT chk_recurring_rule_yearly CHECK ( frequency <> 'YEARLY' OR
                                                 (month_of_year IS NOT NULL AND day_of_month IS NOT NULL) ),
    CONSTRAINT chk_recurring_rule_updated_status CHECK (
        (updated_by IS NULL AND updated_at IS NULL) OR (updated_by IS NOT NULL AND updated_at IS NOT NULL)
        )
);

CREATE INDEX idx_recurring_rule_admin_active ON recurring_transaction_rule (admin_user_id, is_active);

CREATE TABLE recurring_transaction_execution
(
    id                       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    rule_id                  BIGINT      NOT NULL,
    occurrence_date          DATE        NOT NULL,
    occurrence_time          TIME        NOT NULL,
    financial_transaction_id BIGINT,
    created_at               TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_recurring_execution_rule FOREIGN KEY (rule_id)
        REFERENCES recurring_transaction_rule (id) ON DELETE CASCADE,
    CONSTRAINT fk_recurring_execution_transaction FOREIGN KEY (financial_transaction_id)
        REFERENCES financial_transactions (id) ON DELETE SET NULL,
    CONSTRAINT uq_recurring_execution_occurrence UNIQUE (rule_id, occurrence_date, occurrence_time),
    CONSTRAINT uq_recurring_execution_transaction UNIQUE (financial_transaction_id)
);

CREATE INDEX idx_recurring_execution_rule_date
    ON recurring_transaction_execution (rule_id, occurrence_date DESC, occurrence_time DESC);
