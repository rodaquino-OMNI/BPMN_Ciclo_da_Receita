package com.hospital.delegates.eligibility;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.inject.Named;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Delegate to validate insurance policy status and expiration.
 *
 * This delegate verifies that insurance policies are active and not expired,
 * ensuring claims can be processed without rejection.
 *
 * @author Hospital Revenue Cycle Team
 * @version 1.0.0
 */
@Component
@Named("validateInsuranceDelegate")
public class ValidateInsuranceDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateInsuranceDelegate.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        LOGGER.info("Validating insurance for process instance: {}",
            execution.getProcessInstanceId());

        try {
            // Get input variables
            String policyNumber = (String) execution.getVariable("policyNumber");
            String policyExpirationDate = (String) execution.getVariable("policyExpirationDate");
            String policyStatus = (String) execution.getVariable("policyStatus");

            LOGGER.debug("Validating insurance - Policy: {}, Expiration: {}, Status: {}",
                policyNumber, policyExpirationDate, policyStatus);

            // Validate policy
            boolean isValid = validatePolicy(policyNumber, policyExpirationDate, policyStatus);
            String validationMessage = getValidationMessage(isValid, policyExpirationDate);

            // Set output variables
            execution.setVariable("insuranceValid", isValid);
            execution.setVariable("validationMessage", validationMessage);
            execution.setVariable("validationDate", LocalDate.now().toString());

            LOGGER.info("Insurance validation completed - Valid: {}, Message: {}",
                isValid, validationMessage);

        } catch (Exception e) {
            LOGGER.error("Error validating insurance: {}", e.getMessage(), e);
            execution.setVariable("validationError", e.getMessage());
            execution.setVariable("insuranceValid", false);
            throw e;
        }
    }

    private boolean validatePolicy(String policyNumber, String expirationDate, String status) {
        if (policyNumber == null || policyNumber.isEmpty()) {
            return false;
        }

        if (!"ACTIVE".equalsIgnoreCase(status)) {
            return false;
        }

        if (expirationDate != null) {
            try {
                LocalDate expDate = LocalDate.parse(expirationDate, DATE_FORMATTER);
                if (expDate.isBefore(LocalDate.now())) {
                    return false;
                }
            } catch (Exception e) {
                LOGGER.warn("Invalid expiration date format: {}", expirationDate);
                return false;
            }
        }

        return true;
    }

    private String getValidationMessage(boolean isValid, String expirationDate) {
        if (isValid) {
            return "Insurance policy is active and valid";
        }

        if (expirationDate != null) {
            try {
                LocalDate expDate = LocalDate.parse(expirationDate, DATE_FORMATTER);
                if (expDate.isBefore(LocalDate.now())) {
                    return "Insurance policy has expired";
                }
            } catch (Exception e) {
                // Handled in validation
            }
        }

        return "Insurance policy is invalid or inactive";
    }
}
