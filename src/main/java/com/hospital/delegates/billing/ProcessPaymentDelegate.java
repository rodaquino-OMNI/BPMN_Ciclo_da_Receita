package com.hospital.delegates.billing;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.inject.Named;

/**
 * Delegate to process insurance payment and patient balance.
 *
 * This delegate handles the reconciliation of insurance payments with claims,
 * calculates patient responsibility, and determines if additional patient billing is required.
 *
 * @author Hospital Revenue Cycle Team
 * @version 1.0.0
 */
@Component
@Named("processPaymentDelegate")
public class ProcessPaymentDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessPaymentDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        LOGGER.info("Processing payment for process instance: {}",
            execution.getProcessInstanceId());

        try {
            // Get input variables
            String claimId = (String) execution.getVariable("claimId");
            Double claimTotalAmount = (Double) execution.getVariable("claimTotalAmount");
            Double insurancePayment = (Double) execution.getVariable("insurancePayment");
            Double adjustments = (Double) execution.getVariable("adjustments");

            LOGGER.debug("Processing payment - Claim: {}, Total: {}, Payment: {}, Adjustments: {}",
                claimId, claimTotalAmount, insurancePayment, adjustments);

            // Process payment
            PaymentResult result = processPayment(claimTotalAmount, insurancePayment, adjustments);

            // Set output variables
            execution.setVariable("paymentProcessed", true);
            execution.setVariable("insurancePaymentAmount", result.insurancePayment);
            execution.setVariable("patientBalance", result.patientBalance);
            execution.setVariable("totalAdjustments", result.adjustments);
            execution.setVariable("paymentDate", result.paymentDate);
            execution.setVariable("requiresPatientBilling", result.requiresPatientBilling);

            LOGGER.info("Payment processed - Insurance: {}, Patient balance: {}",
                result.insurancePayment, result.patientBalance);

        } catch (Exception e) {
            LOGGER.error("Error processing payment: {}", e.getMessage(), e);
            execution.setVariable("paymentError", e.getMessage());
            execution.setVariable("paymentProcessed", false);
            throw e;
        }
    }

    private PaymentResult processPayment(Double total, Double insurance, Double adjustments) {
        // Payment processing logic
        PaymentResult result = new PaymentResult();
        result.insurancePayment = insurance != null ? insurance : 0.0;
        result.adjustments = adjustments != null ? adjustments : 0.0;
        result.patientBalance = (total != null ? total : 0.0) - result.insurancePayment - result.adjustments;
        result.paymentDate = java.time.LocalDateTime.now().toString();
        result.requiresPatientBilling = result.patientBalance > 0;
        return result;
    }

    private static class PaymentResult {
        Double insurancePayment;
        Double patientBalance;
        Double adjustments;
        String paymentDate;
        Boolean requiresPatientBilling;
    }
}
