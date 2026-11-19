CREATE TABLE financial_report_delivery
(
    id            BIGSERIAL PRIMARY KEY,
    admin_user_id BIGINT      NOT NULL REFERENCES users (id),
    frequency     VARCHAR(16) NOT NULL,
    period_start  DATE        NOT NULL,
    period_end    DATE        NOT NULL,
    status        VARCHAR(16) NOT NULL,
    attempted_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    queued_at     TIMESTAMP,
    error_message TEXT,
    CONSTRAINT uq_financial_report_delivery UNIQUE (admin_user_id, frequency, period_start, period_end)
);
