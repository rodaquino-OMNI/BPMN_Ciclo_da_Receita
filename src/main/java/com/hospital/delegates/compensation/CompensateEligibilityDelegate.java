package com.hospital.delegates.compensation;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.inject.Named;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Compensation Handler for Eligibility Check Reversal
 *
 * Clears eligibility verification results when process requires rollback.
 * Implements SAGA pattern compensation for SUB_04_Eligibility_Check process.
 */
@Component
@Named("compensateEligibilityDelegate")
public class CompensateEligibilityDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompensateEligibilityDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String processInstanceId = execution.getProcessInstanceId();

        LOGGER.warn("COMPENSATION TRIGGERED: Reversing eligibility check for process: {}", processInstanceId);

        try {
            // Check if compensation already performed (idempotency)
            Boolean compensationCompleted = (Boolean) execution.getVariable("eligibilityCompensationCompleted");
            if (Boolean.TRUE.equals(compensationCompleted)) {
                LOGGER.info("COMPENSATION SKIPPED: Eligibility already compensated for process: {}", processInstanceId);
                return;
            }

            // Retrieve eligibility data before clearing
            String patientId = (String) execution.getVariable("patientId");
            String beneficiaryId = (String) execution.getVariable("beneficiaryId");
            String healthPlanId = (String) execution.getVariable("healthPlanId");
            Boolean eligibilityConfirmed = (Boolean) execution.getVariable("eligibilityConfirmed");
            String eligibilityStatus = (String) execution.getVariable("eligibilityStatus");

            LOGGER.warn("COMPENSATION DETAILS: Patient: {}, Beneficiary: {}, Plan: {}, Status: {}",
                patientId, beneficiaryId, healthPlanId, eligibilityStatus);

            // Clear eligibility check results
            clearEligibilityResults(execution, patientId, beneficiaryId);

            // Clear provider response cache
            clearProviderResponseCache(execution, healthPlanId);

            // Create audit trail
            createEligibilityCompensationAudit(execution, patientId, beneficiaryId,
                "Eligibility check results cleared");

            // Mark compensation as completed
            execution.setVariable("eligibilityCompensationCompleted", true);
            execution.setVariable("eligibilityCompensationReason", "Eligibility check reverted due to process rollback");
            execution.setVariable("eligibilityCompensationTimestamp", LocalDateTime.now().toString());

            LOGGER.warn("COMPENSATION COMPLETED: Eligibility cleared for patient {} in process: {}",
                patientId, processInstanceId);

        } catch (Exception e) {
            LOGGER.error("COMPENSATION FAILED for process {}: {}", processInstanceId, e.getMessage(), e);

            // Log failure but don't throw - compensation failures shouldn't block process
            execution.setVariable("eligibilityCompensationCompleted", false);
            execution.setVariable("eligibilityCompensationError", e.getMessage());

            // Create failure audit
            createEligibilityCompensationAudit(execution,
                (String) execution.getVariable("patientId"),
                (String) execution.getVariable("beneficiaryId"),
                "Compensation failed: " + e.getMessage());
        }
    }

    /**
     * Clears all eligibility check results and resets status
     */
    private void clearEligibilityResults(DelegateExecution execution, String patientId,
                                        String beneficiaryId) {
        LOGGER.warn("Clearing eligibility results - Patient: {}, Beneficiary: {}",
            patientId, beneficiaryId);

        // Store original results for audit before clearing
        Map<String, Object> originalResults = new HashMap<>();
        originalResults.put("eligibilityConfirmed", execution.getVariable("eligibilityConfirmed"));
        originalResults.put("eligibilityStatus", execution.getVariable("eligibilityStatus"));
        originalResults.put("coverageDetails", execution.getVariable("coverageDetails"));
        originalResults.put("verificationDate", execution.getVariable("verificationDate"));
        execution.setVariable("originalEligibilityBeforeCompensation", originalResults);

        // Clear eligibility results
        execution.removeVariable("eligibilityConfirmed");
        execution.removeVariable("eligibilityStatus");
        execution.removeVariable("coverageDetails");
        execution.removeVariable("coverageStartDate");
        execution.removeVariable("coverageEndDate");
        execution.removeVariable("planType");
        execution.removeVariable("copayAmount");
        execution.removeVariable("deductibleAmount");
        execution.removeVariable("verificationDate");
        execution.removeVariable("verificationResponse");

        // Reset eligibility status to PENDING
        execution.setVariable("eligibilityStatus", "PENDING");
        execution.setVariable("eligibilityConfirmed", false);
        execution.setVariable("eligibilityReversalDate", LocalDateTime.now().toString());

        // TODO: Call eligibility service to clear verification
        // eligibilityService.clearVerification(patientId, beneficiaryId);

        LOGGER.info("Eligibility results cleared for beneficiary: {}", beneficiaryId);
    }

    /**
     * Clears provider response cache to force re-verification if needed
     */
    private void clearProviderResponseCache(DelegateExecution execution, String healthPlanId) {
        LOGGER.warn("Clearing provider response cache - Health Plan: {}", healthPlanId);

        // Store cache info for audit
        execution.setVariable("cachedResponseClearedForPlan", healthPlanId);

        // Clear cache variables
        execution.removeVariable("providerResponse");
        execution.removeVariable("providerResponseCache");
        execution.removeVariable("cacheTimestamp");

        // TODO: Call cache service to clear provider responses
        // cacheService.clearProviderResponses(healthPlanId);

        LOGGER.info("Provider response cache cleared for plan: {}", healthPlanId);
    }

    /**
     * Creates audit trail for eligibility compensation
     */
    private void createEligibilityCompensationAudit(DelegateExecution execution, String patientId,
                                                   String beneficiaryId, String reason) {
        Map<String, Object> auditRecord = new HashMap<>();
        auditRecord.put("compensationType", "ELIGIBILITY_REVERSAL");
        auditRecord.put("patientId", patientId);
        auditRecord.put("beneficiaryId", beneficiaryId);
        auditRecord.put("processInstanceId", execution.getProcessInstanceId());
        auditRecord.put("reason", reason);
        auditRecord.put("timestamp", LocalDateTime.now().toString());
        auditRecord.put("activityId", execution.getCurrentActivityId());
        auditRecord.put("originalResults", execution.getVariable("originalEligibilityBeforeCompensation"));

        execution.setVariable("eligibilityCompensationAuditRecord", auditRecord);

        LOGGER.warn("COMPENSATION AUDIT: {} - Patient: {}, Beneficiary: {}, Process: {}",
            reason, patientId, beneficiaryId, execution.getProcessInstanceId());
    }
}
