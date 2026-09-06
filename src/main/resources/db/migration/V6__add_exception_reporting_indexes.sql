CREATE INDEX IF NOT EXISTS idx_exception_records_severity
    ON exception_records (severity);

CREATE INDEX IF NOT EXISTS idx_financial_transactions_customer_id
    ON financial_transactions (customer_id);

CREATE INDEX IF NOT EXISTS idx_exception_records_rule_type
    ON exception_records (rule_type);

CREATE INDEX IF NOT EXISTS idx_exception_records_transaction_id_reporting
    ON exception_records (transaction_id);
