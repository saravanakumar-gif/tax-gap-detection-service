CREATE TABLE tax_result
(
    id UUID PRIMARY KEY,

    transaction_id UUID NOT NULL UNIQUE,

    expected_tax NUMERIC(19, 4),

    tax_gap NUMERIC(19, 4),

    compliance_status VARCHAR(30) NOT NULL,

    calculated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_tax_result_transaction
        FOREIGN KEY (transaction_id)
            REFERENCES financial_transactions (id)
);

CREATE INDEX idx_tax_result_transaction_id
    ON tax_result (transaction_id);

CREATE INDEX idx_tax_result_compliance_status
    ON tax_result (compliance_status);