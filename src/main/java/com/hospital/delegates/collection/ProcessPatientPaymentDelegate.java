package com.hospital.delegates.collection;

import com.hospital.services.idempotency.IdempotencyException;
import com.hospital.services.idempotency.IdempotencyService;
import com.hospital.services.idempotency.IdempotencyKeyGenerator;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.inject.Named;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Delegate to process patient payment and update account balance
 * Implements idempotency protection to prevent duplicate payment processing
 */
@Component
@Named("processPatientPaymentDelegate")
public class ProcessPatientPaymentDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessPatientPaymentDelegate.class);

    @Autowired
    private IdempotencyService idempotencyService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String processInstanceId = execution.getProcessInstanceId();
        LOGGER.info("Processing patient payment for process instance: {}", processInstanceId);

        try {
            // Get input variables
            String patientId = getStringVariable(execution, "patientId");
            Double patientBalance = getDoubleVariable(execution, "patientBalance");
            Double paymentAmount = getDoubleVariable(execution, "paymentAmount");
            String paymentMethod = getStringVariable(execution, "paymentMethod");

            // Validate inputs
            validateInputs(patientId, paymentAmount, paymentMethod);

            LOGGER.debug("Processing payment - Patient: {}, Balance: {}, Payment: {}, Method: {}",
                patientId, patientBalance, paymentAmount, paymentMethod);

            // Generate idempotency key using payment processing method
            // Creating a deterministic transaction reference from execution ID
            String transactionReference = execution.getId() + "-payment";
            String idempotencyKey = IdempotencyKeyGenerator.generatePaymentProcessingKey(
                patientId,
                transactionReference
            );

            LOGGER.info("Generated idempotency key: {} for payment processing", idempotencyKey);

            // Execute payment with idempotency protection
            PaymentResult result = executePaymentWithIdempotency(
                idempotencyKey,
                patientId,
                patientBalance,
                paymentAmount,
                paymentMethod,
                processInstanceId
            );

            // Set output variables
            setOutputVariables(execution, result);

            LOGGER.info("Payment processed successfully - Transaction: {}, Success: {}, Remaining balance: {}, Paid in full: {}",
                result.transactionId, result.success, result.remainingBalance, result.paidInFull);

        } catch (IdempotencyException e) {
            handleIdempotencyException(execution, e);
        } catch (IllegalArgumentException e) {
            handleValidationException(execution, e);
        } catch (Exception e) {
            handleGeneralException(execution, e);
        }
    }

    /**
     * Safely retrieve String variable from execution context
     */
    private String getStringVariable(DelegateExecution execution, String variableName) {
        Object value = execution.getVariable(variableName);
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    /**
     * Safely retrieve Double variable from execution context
     * Handles Integer, Long, BigDecimal, and String conversions
     */
    private Double getDoubleVariable(DelegateExecution execution, String variableName) {
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
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                LOGGER.warn("Failed to convert variable '{}' with value '{}' to Double", variableName, value);
                return null;
            }
        }
        LOGGER.warn("Variable '{}' has unexpected type: {}", variableName, value.getClass().getName());
        return null;
    }

    /**
     * Execute payment with idempotency protection
     */
    private PaymentResult executePaymentWithIdempotency(
            String idempotencyKey,
            String patientId,
            Double patientBalance,
            Double paymentAmount,
            String paymentMethod,
            String processInstanceId) {

        try {
            // Execute payment with idempotency protection
            LOGGER.info("Executing payment with idempotency protection - Key: {}", idempotencyKey);

            PaymentResult result = idempotencyService.executeIdempotent(
                "PAYMENT",
                idempotencyKey,
                () -> processPaymentInternal(patientId, patientBalance, paymentAmount, paymentMethod, processInstanceId)
            );

            LOGGER.info("Payment execution completed - Transaction: {}", result.transactionId);
            return result;

        } catch (IdempotencyException e) {
            LOGGER.error("Idempotency violation detected - Key: {}, Type: {}",
                idempotencyKey, e.getOperationType(), e);
            throw e;
        }
    }

    /**
     * Internal payment processing logic
     * This method executes the actual payment transaction
     */
    private PaymentResult processPaymentInternal(
            String patientId,
            Double balance,
            Double payment,
            String method,
            String processInstanceId) {

        LOGGER.info("Executing internal payment processing - Patient: {}, Amount: {}", patientId, payment);

        // Simulated payment processing
        // TODO: Replace with actual payment gateway integration
        PaymentResult result = new PaymentResult();
        result.transactionId = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        result.success = true;
        result.processInstanceId = processInstanceId;
        result.patientId = patientId;
        result.paymentMethod = method;

        double currentBalance = balance != null ? balance : 0.0;
        double paymentAmt = payment != null ? payment : 0.0;

        result.paymentAmount = paymentAmt;
        result.previousBalance = currentBalance;
        result.remainingBalance = Math.max(0.0, currentBalance - paymentAmt);
        result.paidInFull = result.remainingBalance == 0.0;
        result.confirmationDate = java.time.LocalDateTime.now().toString();

        LOGGER.debug("Payment processed internally - Transaction: {}, Previous: {}, Paid: {}, Remaining: {}",
            result.transactionId, result.previousBalance, result.paymentAmount, result.remainingBalance);

        return result;
    }

    /**
     * Validate input parameters
     */
    private void validateInputs(String patientId, Double paymentAmount, String paymentMethod) {
        if (patientId == null || patientId.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient ID is required for payment processing");
        }

        if (paymentAmount == null || paymentAmount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method is required");
        }
    }

    /**
     * Set output variables in process execution
     */
    private void setOutputVariables(DelegateExecution execution, PaymentResult result) {
        execution.setVariable("paymentTransactionId", result.transactionId);
        execution.setVariable("paymentProcessedSuccessfully", result.success);
        execution.setVariable("remainingBalance", result.remainingBalance);
        execution.setVariable("paymentConfirmationDate", result.confirmationDate);
        execution.setVariable("accountPaidInFull", result.paidInFull);

        // Update patient balance
        execution.setVariable("patientBalance", result.remainingBalance);

        LOGGER.debug("Output variables set - Transaction: {}, Success: {}, Balance: {}",
            result.transactionId, result.success, result.remainingBalance);
    }

    /**
     * Handle idempotency exceptions
     */
    private void handleIdempotencyException(DelegateExecution execution, IdempotencyException e) {
        LOGGER.error("Idempotency error in payment processing - Key: {}, Type: {}, Message: {}",
            e.getOperationKey(), e.getOperationType(), e.getMessage());

        execution.setVariable("paymentProcessingError", "Idempotency violation: " + e.getMessage());
        execution.setVariable("paymentProcessedSuccessfully", false);
        execution.setVariable("idempotencyKey", e.getOperationKey());
        execution.setVariable("idempotencyType", e.getOperationType());

        throw new BpmnError("IDEMPOTENCY_ERROR",
            "Idempotency violation in payment processing: " + e.getMessage());
    }

    /**
     * Handle validation exceptions
     */
    private void handleValidationException(DelegateExecution execution, IllegalArgumentException e) {
        LOGGER.error("Validation error in payment processing: {}", e.getMessage());

        execution.setVariable("paymentProcessingError", "Validation error: " + e.getMessage());
        execution.setVariable("paymentProcessedSuccessfully", false);

        throw new BpmnError("PAYMENT_VALIDATION_ERROR",
            "Payment validation failed: " + e.getMessage());
    }

    /**
     * Handle general exceptions
     */
    private void handleGeneralException(DelegateExecution execution, Exception e) {
        LOGGER.error("Error processing patient payment: {}", e.getMessage(), e);

        execution.setVariable("paymentProcessingError", e.getMessage());
        execution.setVariable("paymentProcessedSuccessfully", false);

        throw new BpmnError("PAYMENT_PROCESSING_ERROR",
            "Payment processing failed: " + e.getMessage());
    }

    /**
     * Payment result data transfer object
     * Made public for JSON serialization by IdempotencyService
     */
    public static class PaymentResult {
        public String transactionId;
        public Boolean success;
        public Double remainingBalance;
        public String confirmationDate;
        public Boolean paidInFull;
        public String processInstanceId;
        public String patientId;
        public String paymentMethod;
        public Double paymentAmount;
        public Double previousBalance;

        // Default constructor for JSON deserialization
        public PaymentResult() {
        }
    }
}
