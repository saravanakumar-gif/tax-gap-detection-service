CREATE TABLE compliance_rules (
    id UUID PRIMARY KEY,
    rule_code VARCHAR(100) NOT NULL UNIQUE,
    rule_name VARCHAR(200) NOT NULL,
    rule_type VARCHAR(60) NOT NULL,
    enabled BOOLEAN NOT NULL,
    configuration JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE exception_records (
    id UUID PRIMARY KEY,
    transaction_id UUID NOT NULL,
    rule_id UUID NOT NULL,
    rule_code VARCHAR(100) NOT NULL,
    rule_type VARCHAR(60) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    message VARCHAR(500) NOT NULL,
    details JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_exception_records_transaction
        FOREIGN KEY (transaction_id)
            REFERENCES financial_transactions (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_exception_records_rule
        FOREIGN KEY (rule_id)
            REFERENCES compliance_rules (id)
);

CREATE INDEX idx_compliance_rules_enabled
    ON compliance_rules (enabled);

CREATE INDEX idx_compliance_rules_rule_type
    ON compliance_rules (rule_type);

CREATE INDEX idx_exception_records_transaction_id
    ON exception_records (transaction_id);

CREATE INDEX idx_exception_records_rule_code
    ON exception_records (rule_code);

INSERT INTO compliance_rules (
    id,
    rule_code,
    rule_name,
    rule_type,
    enabled,
    configuration,
    created_at,
    updated_at
) VALUES
(
    gen_random_uuid(),
    'HIGH_VALUE_TRANSACTION',
    'High-Value Transaction Rule',
    'HIGH_VALUE_TRANSACTION',
    true,
    '{"threshold": 100000.00, "severity": "HIGH"}'::jsonb,
    now(),
    now()
),
(
    gen_random_uuid(),
    'REFUND_VALIDATION',
    'Refund Validation Rule',
    'REFUND_VALIDATION',
    true,
    '{"originalSaleTransactionIdField": "originalTransactionId", "severity": "HIGH"}'::jsonb,
    now(),
    now()
),
(
    gen_random_uuid(),
    'GST_SLAB_VIOLATION',
    'GST Slab Violation Rule',
    'GST_SLAB_VIOLATION',
    true,
    '{"slabThreshold": 1000.00, "requiredTaxRate": 18.00, "severity": "HIGH"}'::jsonb,
    now(),
    now()
);
