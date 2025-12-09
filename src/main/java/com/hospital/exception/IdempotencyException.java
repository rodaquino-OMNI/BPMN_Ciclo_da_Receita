package com.hospital.exception;

/**
 * Exception thrown when idempotency violations occur
 */
public class IdempotencyException extends RuntimeException {

    private final String idempotencyKey;
    private final String operationType;
    private final String status;

    public IdempotencyException(String message, String idempotencyKey, String operationType) {
        super(message);
        this.idempotencyKey = idempotencyKey;
        this.operationType = operationType;
        this.status = "UNKNOWN";
    }

    public IdempotencyException(String message, String idempotencyKey, String operationType, String status) {
        super(message);
        this.idempotencyKey = idempotencyKey;
        this.operationType = operationType;
        this.status = status;
    }

    public IdempotencyException(String message, Throwable cause, String idempotencyKey, String operationType) {
        super(message, cause);
        this.idempotencyKey = idempotencyKey;
        this.operationType = operationType;
        this.status = "UNKNOWN";
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getOperationType() {
        return operationType;
    }

    public String getStatus() {
        return status;
    }
}
