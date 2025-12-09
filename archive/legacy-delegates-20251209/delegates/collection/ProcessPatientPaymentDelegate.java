package com.hospital.delegates.collection;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;

/**
 * Delegate to process patient payment and update account balance
 */
public class ProcessPatientPaymentDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessPatientPaymentDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        LOGGER.info("Processing patient payment for process instance: {}",
            execution.getProcessInstanceId());

        try {
            // Get input variables
            String patientId = (String) execution.getVariable("patientId");
            Double patientBalance = (Double) execution.getVariable("patientBalance");
            Double paymentAmount = (Double) execution.getVariable("paymentAmount");
            String paymentMethod = (String) execution.getVariable("paymentMethod");

            LOGGER.debug("Processing payment - Patient: {}, Balance: {}, Payment: {}, Method: {}",
                patientId, patientBalance, paymentAmount, paymentMethod);

            // Process payment
            PaymentResult result = processPayment(patientId, patientBalance,
                paymentAmount, paymentMethod);

            // Set output variables
            execution.setVariable("paymentTransactionId", result.transactionId);
            execution.setVariable("paymentProcessedSuccessfully", result.success);
            execution.setVariable("remainingBalance", result.remainingBalance);
            execution.setVariable("paymentConfirmationDate", result.confirmationDate);
            execution.setVariable("accountPaidInFull", result.paidInFull);

            // Update patient balance
            execution.setVariable("patientBalance", result.remainingBalance);

            LOGGER.info("Payment processed - Success: {}, Remaining balance: {}, Paid in full: {}",
                result.success, result.remainingBalance, result.paidInFull);

        } catch (Exception e) {
            LOGGER.error("Error processing patient payment: {}", e.getMessage(), e);
            execution.setVariable("paymentProcessingError", e.getMessage());
            execution.setVariable("paymentProcessedSuccessfully", false);
            throw e;
        }
    }

    private PaymentResult processPayment(String patientId, Double balance,
            Double payment, String method) {
        // Simulated payment processing
        // TODO: Replace with actual payment gateway integration
        PaymentResult result = new PaymentResult();
        result.transactionId = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        result.success = true;

        double currentBalance = balance != null ? balance : 0.0;
        double paymentAmt = payment != null ? payment : 0.0;
        result.remainingBalance = Math.max(0.0, currentBalance - paymentAmt);
        result.paidInFull = result.remainingBalance == 0.0;
        result.confirmationDate = java.time.LocalDateTime.now().toString();

        return result;
    }

    private static class PaymentResult {
        String transactionId;
        Boolean success;
        Double remainingBalance;
        String confirmationDate;
        Boolean paidInFull;
    }
}
