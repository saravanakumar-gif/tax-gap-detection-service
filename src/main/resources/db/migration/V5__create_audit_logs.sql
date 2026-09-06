CREATE TABLE audit_logs (
                            id UUID PRIMARY KEY,
                            event_type VARCHAR(30) NOT NULL,
                            transaction_id VARCHAR(100) NOT NULL,
                            event_timestamp TIMESTAMPTZ NOT NULL,
                            detail_json JSONB NOT NULL
);

CREATE INDEX idx_audit_logs_transaction_id
    ON audit_logs (transaction_id);

CREATE INDEX idx_audit_logs_event_type
    ON audit_logs (event_type);

CREATE INDEX idx_audit_logs_event_timestamp
    ON audit_logs (event_timestamp);