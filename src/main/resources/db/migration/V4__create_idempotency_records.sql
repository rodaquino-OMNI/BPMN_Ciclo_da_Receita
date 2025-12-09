-- Create idempotency_records table for tracking operation idempotency
CREATE TABLE idempotency_records (
    id BIGSERIAL PRIMARY KEY,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    operation_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    request_payload TEXT,
    response_payload TEXT,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    process_instance_id VARCHAR(100),
    retry_count INTEGER DEFAULT 0,
    CONSTRAINT chk_status CHECK (status IN ('PROCESSING', 'COMPLETED', 'FAILED'))
);

-- Create indexes for performance
CREATE UNIQUE INDEX idx_idempotency_key ON idempotency_records(idempotency_key);
CREATE INDEX idx_operation_type ON idempotency_records(operation_type);
CREATE INDEX idx_created_at ON idempotency_records(created_at);
CREATE INDEX idx_status ON idempotency_records(status);
CREATE INDEX idx_process_instance_id ON idempotency_records(process_instance_id);
CREATE INDEX idx_expires_at ON idempotency_records(expires_at);

-- Add comment to table
COMMENT ON TABLE idempotency_records IS 'Tracks operation idempotency to prevent duplicate execution of critical operations like payments';
COMMENT ON COLUMN idempotency_records.idempotency_key IS 'Unique key identifying the operation (SHA-256 hash)';
COMMENT ON COLUMN idempotency_records.operation_type IS 'Type of operation (e.g., PAYMENT, INVOICE)';
COMMENT ON COLUMN idempotency_records.status IS 'Current status: PROCESSING, COMPLETED, or FAILED';
COMMENT ON COLUMN idempotency_records.response_payload IS 'Cached result for completed operations (JSON)';
COMMENT ON COLUMN idempotency_records.expires_at IS 'When this record expires and can be cleaned up';
COMMENT ON COLUMN idempotency_records.retry_count IS 'Number of retry attempts for failed operations';
