package com.hospital.delegates.coding.exceptions;

import java.util.List;
import java.util.ArrayList;

/**
 * Custom exception for medical code validation errors
 */
public class ValidationException extends RuntimeException {

    private final String errorType;
    private final String severity;
    private final List<String> validationErrors;
    private final String actionRequired;

    public ValidationException(String message) {
        super(message);
        this.errorType = "VALIDATION_ERROR";
        this.severity = "ERROR";
        this.validationErrors = new ArrayList<>();
        this.actionRequired = "REVIEW_REQUIRED";
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.errorType = "VALIDATION_ERROR";
        this.severity = "ERROR";
        this.validationErrors = new ArrayList<>();
        this.actionRequired = "REVIEW_REQUIRED";
    }

    public ValidationException(String message, String errorType, String severity,
                             List<String> validationErrors, String actionRequired) {
        super(message);
        this.errorType = errorType;
        this.severity = severity;
        this.validationErrors = validationErrors != null ? new ArrayList<>(validationErrors) : new ArrayList<>();
        this.actionRequired = actionRequired;
    }

    public ValidationException(String message, String errorType, String severity,
                             List<String> validationErrors, String actionRequired, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
        this.severity = severity;
        this.validationErrors = validationErrors != null ? new ArrayList<>(validationErrors) : new ArrayList<>();
        this.actionRequired = actionRequired;
    }

    public String getErrorType() {
        return errorType;
    }

    public String getSeverity() {
        return severity;
    }

    public List<String> getValidationErrors() {
        return new ArrayList<>(validationErrors);
    }

    public String getActionRequired() {
        return actionRequired;
    }

    public boolean isCritical() {
        return "ERROR".equals(severity);
    }

    @Override
    public String toString() {
        return String.format("ValidationException[errorType=%s, severity=%s, errors=%d, action=%s, message=%s]",
            errorType, severity, validationErrors.size(), actionRequired, getMessage());
    }
}
