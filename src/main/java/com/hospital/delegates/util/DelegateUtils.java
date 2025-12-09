package com.hospital.delegates.util;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.BpmnError;

import java.util.List;

/**
 * Utility class for common delegate operations.
 * Provides null-safe methods for extracting process variables and validation.
 *
 * @author Hospital Revenue Cycle Team
 * @version 1.0.0
 */
public class DelegateUtils {

    private DelegateUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Gets a String variable with null safety.
     *
     * @param execution the delegate execution
     * @param variableName the variable name
     * @return the variable value or null if not present
     */
    public static String getString(DelegateExecution execution, String variableName) {
        Object value = execution.getVariable(variableName);
        return value != null ? value.toString() : null;
    }

    /**
     * Gets a required String variable with validation.
     *
     * @param execution the delegate execution
     * @param variableName the variable name
     * @param errorCode the error code for BpmnError
     * @return the variable value
     * @throws BpmnError if variable is null or empty
     */
    public static String getRequiredString(DelegateExecution execution, String variableName, String errorCode) {
        String value = getString(execution, variableName);
        if (value == null || value.trim().isEmpty()) {
            throw new BpmnError(errorCode, variableName + " is required");
        }
        return value;
    }

    /**
     * Gets a Double variable with null safety.
     *
     * @param execution the delegate execution
     * @param variableName the variable name
     * @return the variable value or null if not present
     */
    public static Double getDouble(DelegateExecution execution, String variableName) {
        Object value = execution.getVariable(variableName);
        if (value == null) {
            return null;
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Gets a required Double variable with validation.
     *
     * @param execution the delegate execution
     * @param variableName the variable name
     * @param errorCode the error code for BpmnError
     * @return the variable value
     * @throws BpmnError if variable is null or invalid
     */
    public static Double getRequiredDouble(DelegateExecution execution, String variableName, String errorCode) {
        Double value = getDouble(execution, variableName);
        if (value == null) {
            throw new BpmnError(errorCode, variableName + " is required and must be a valid number");
        }
        return value;
    }

    /**
     * Gets a Boolean variable with null safety.
     *
     * @param execution the delegate execution
     * @param variableName the variable name
     * @param defaultValue the default value if not present
     * @return the variable value or default if not present
     */
    public static Boolean getBoolean(DelegateExecution execution, String variableName, boolean defaultValue) {
        Object value = execution.getVariable(variableName);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(value.toString());
    }

    /**
     * Gets a List variable with null safety and type checking.
     *
     * @param execution the delegate execution
     * @param variableName the variable name
     * @param <T> the list element type
     * @return the variable value or null if not present
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getList(DelegateExecution execution, String variableName) {
        Object value = execution.getVariable(variableName);
        if (value instanceof List) {
            return (List<T>) value;
        }
        return null;
    }

    /**
     * Validates that a string matches a pattern.
     *
     * @param value the value to validate
     * @param pattern the regex pattern
     * @param fieldName the field name for error messages
     * @param errorCode the error code for BpmnError
     * @throws BpmnError if validation fails
     */
    public static void validatePattern(String value, String pattern, String fieldName, String errorCode) {
        if (value == null || !value.matches(pattern)) {
            throw new BpmnError(errorCode, fieldName + " format is invalid");
        }
    }

    /**
     * Validates that a value is not null.
     *
     * @param value the value to validate
     * @param fieldName the field name for error messages
     * @param errorCode the error code for BpmnError
     * @throws BpmnError if value is null
     */
    public static void validateNotNull(Object value, String fieldName, String errorCode) {
        if (value == null) {
            throw new BpmnError(errorCode, fieldName + " is required");
        }
    }

    /**
     * Validates that a string is not null or empty.
     *
     * @param value the value to validate
     * @param fieldName the field name for error messages
     * @param errorCode the error code for BpmnError
     * @throws BpmnError if value is null or empty
     */
    public static void validateNotEmpty(String value, String fieldName, String errorCode) {
        if (value == null || value.trim().isEmpty()) {
            throw new BpmnError(errorCode, fieldName + " is required and cannot be empty");
        }
    }

    /**
     * Validates that a number is positive.
     *
     * @param value the value to validate
     * @param fieldName the field name for error messages
     * @param errorCode the error code for BpmnError
     * @throws BpmnError if value is null or not positive
     */
    public static void validatePositive(Double value, String fieldName, String errorCode) {
        if (value == null || value <= 0) {
            throw new BpmnError(errorCode, fieldName + " must be a positive number");
        }
    }
}
