CREATE TABLE transaction_batches (
    id UUID PRIMARY KEY,
    batch_reference VARCHAR(64) NOT NULL UNIQUE,
    uploaded_at TIMESTAMPTZ NOT NULL,
    total_transactions INTEGER NOT NULL,
    successful_transactions INTEGER NOT NULL,
    failed_transactions INTEGER NOT NULL
);

CREATE TABLE financial_transactions (
    id UUID PRIMARY KEY,
    batch_id UUID NOT NULL,
    transaction_id VARCHAR(100),
    transaction_date DATE,
    customer_id VARCHAR(100),
    amount NUMERIC(19, 4),
    tax_rate NUMERIC(9, 4),
    reported_tax NUMERIC(19, 4),
    transaction_type VARCHAR(20),
    validation_status VARCHAR(20) NOT NULL,
    failure_reasons JSONB NOT NULL,
    raw_transaction JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_financial_transactions_batch
        FOREIGN KEY (batch_id)
        REFERENCES transaction_batches (id)
);

CREATE INDEX idx_financial_transactions_batch_id
    ON financial_transactions (batch_id);

CREATE INDEX idx_financial_transactions_validation_status
    ON financial_transactions (validation_status);
