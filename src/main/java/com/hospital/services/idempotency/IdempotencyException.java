package com.hospital.services.idempotency;

import lombok.Getter;

/**
 * Custom exception for idempotency-related errors.
 *
 * <p>This exception is thrown when an idempotent operation cannot be completed,
 * either due to conflicts, serialization errors, or execution failures.</p>
 *
 * @author Hospital Revenue Cycle System
 * @version 1.0
 */
@Getter
public class IdempotencyException extends RuntimeException {

    /**
     * The type of operation that failed.
     */
    private final String operationType;

    /**
     * The operation key that was used.
     */
    private final String operationKey;

    /**
     * The original result that was stored (if available).
     */
    private final String originalResult;

    /**
     * Creates a new IdempotencyException with a message only.
     *
     * @param message the error message
     */
    public IdempotencyException(String message) {
        super(message);
        this.operationType = null;
        this.operationKey = null;
        this.originalResult = null;
    }

    /**
     * Creates a new IdempotencyException with full context.
     *
     * @param message the error message
     * @param operationType the type of operation that failed
     * @param operationKey the operation key
     * @param originalResult the original result (if any)
     */
    public IdempotencyException(
            String message,
            String operationType,
            String operationKey,
            String originalResult) {
        super(message);
        this.operationType = operationType;
        this.operationKey = operationKey;
        this.originalResult = originalResult;
    }

    /**
     * Creates a new IdempotencyException with a cause.
     *
     * @param message the error message
     * @param operationType the type of operation that failed
     * @param operationKey the operation key
     * @param originalResult the original result (if any)
     * @param cause the underlying cause
     */
    public IdempotencyException(
            String message,
            String operationType,
            String operationKey,
            String originalResult,
            Throwable cause) {
        super(message, cause);
        this.operationType = operationType;
        this.operationKey = operationKey;
        this.originalResult = originalResult;
    }

    /**
     * Creates a new IdempotencyException with a cause.
     *
     * @param message the error message
     * @param cause the underlying cause
     */
    public IdempotencyException(String message, Throwable cause) {
        super(message, cause);
        this.operationType = null;
        this.operationKey = null;
        this.originalResult = null;
    }

    /**
     * Returns a detailed error message including operation context.
     *
     * @return a formatted error message
     */
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder(super.getMessage());

        if (operationType != null) {
            sb.append(" [operationType=").append(operationType);

            if (operationKey != null) {
                sb.append(", operationKey=").append(operationKey);
            }

            if (originalResult != null) {
                int maxResultLength = 100;
                String truncatedResult = originalResult.length() > maxResultLength
                    ? originalResult.substring(0, maxResultLength) + "..."
                    : originalResult;
                sb.append(", originalResult=").append(truncatedResult);
            }

            sb.append("]");
        }

        return sb.toString();
    }

    /**
     * Checks if this exception has an original result stored.
     *
     * @return true if an original result is available
     */
    public boolean hasOriginalResult() {
        return originalResult != null && !originalResult.isEmpty();
    }

    /**
     * Checks if this exception has operation context.
     *
     * @return true if operation type and key are available
     */
    public boolean hasOperationContext() {
        return operationType != null && operationKey != null;
    }
}
