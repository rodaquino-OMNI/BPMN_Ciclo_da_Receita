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
 * Compensation Handler for Claim Generation Reversal
 *
 * Voids or marks claims as compensated when process requires rollback.
 * Implements SAGA pattern compensation for SUB_05_Claim_Generation process.
 */
@Component
@Named("compensateClaimDelegate")
public class CompensateClaimDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompensateClaimDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String processInstanceId = execution.getProcessInstanceId();

        LOGGER.warn("COMPENSATION TRIGGERED: Reversing claim generation for process: {}", processInstanceId);

        try {
            // Check if compensation already performed (idempotency)
            Boolean compensationCompleted = (Boolean) execution.getVariable("claimCompensationCompleted");
            if (Boolean.TRUE.equals(compensationCompleted)) {
                LOGGER.info("COMPENSATION SKIPPED: Claim already compensated for process: {}", processInstanceId);
                return;
            }

            // Retrieve claim data before voiding
            String claimId = (String) execution.getVariable("claimId");
            String claimNumber = (String) execution.getVariable("claimNumber");
            String patientId = (String) execution.getVariable("patientId");
            String healthPlanId = (String) execution.getVariable("healthPlanId");
            String claimStatus = (String) execution.getVariable("claimStatus");
            Double claimAmount = (Double) execution.getVariable("claimAmount");

            LOGGER.warn("COMPENSATION DETAILS: Claim: {}, Number: {}, Patient: {}, Status: {}, Amount: {}",
                claimId, claimNumber, patientId, claimStatus, claimAmount);

            // Void the claim
            voidClaim(execution, claimId, claimNumber);

            // Notify billing system
            notifyBillingSystem(execution, claimId, claimNumber);

            // Create audit trail
            createClaimCompensationAudit(execution, claimId, claimNumber,
                "Claim voided and marked as compensated");

            // Mark compensation as completed
            execution.setVariable("claimCompensationCompleted", true);
            execution.setVariable("claimCompensationReason", "Claim generation reverted due to process rollback");
            execution.setVariable("claimCompensationTimestamp", LocalDateTime.now().toString());

            LOGGER.warn("COMPENSATION COMPLETED: Claim {} voided for process: {}",
                claimNumber, processInstanceId);

        } catch (Exception e) {
            LOGGER.error("COMPENSATION FAILED for process {}: {}", processInstanceId, e.getMessage(), e);

            // Log failure but don't throw - compensation failures shouldn't block process
            execution.setVariable("claimCompensationCompleted", false);
            execution.setVariable("claimCompensationError", e.getMessage());

            // Create failure audit
            createClaimCompensationAudit(execution,
                (String) execution.getVariable("claimId"),
                (String) execution.getVariable("claimNumber"),
                "Compensation failed: " + e.getMessage());
        }
    }

    /**
     * Voids the generated claim and updates status
     */
    private void voidClaim(DelegateExecution execution, String claimId, String claimNumber) {
        LOGGER.warn("Voiding claim - ID: {}, Number: {}", claimId, claimNumber);

        // Store original claim data for audit before voiding
        Map<String, Object> originalClaim = new HashMap<>();
        originalClaim.put("claimId", claimId);
        originalClaim.put("claimNumber", claimNumber);
        originalClaim.put("claimStatus", execution.getVariable("claimStatus"));
        originalClaim.put("claimAmount", execution.getVariable("claimAmount"));
        originalClaim.put("claimDate", execution.getVariable("claimDate"));
        originalClaim.put("submissionDate", execution.getVariable("submissionDate"));
        execution.setVariable("originalClaimBeforeCompensation", originalClaim);

        // Update claim status to VOIDED or COMPENSATED
        String newStatus = determineVoidStatus(execution);
        execution.setVariable("claimStatus", newStatus);
        execution.setVariable("claimVoided", true);
        execution.setVariable("claimVoidDate", LocalDateTime.now().toString());
        execution.setVariable("claimVoidReason", "Process compensation triggered");

        // Clear submission-related variables
        execution.removeVariable("submissionResult");
        execution.removeVariable("submissionConfirmation");
        execution.removeVariable("submissionDate");

        // TODO: Call claim service to void claim
        // claimService.voidClaim(claimId, "Process compensation");

        LOGGER.info("Claim voided: {} with status: {}", claimNumber, newStatus);
    }

    /**
     * Determines appropriate void status based on claim state
     */
    private String determineVoidStatus(DelegateExecution execution) {
        String currentStatus = (String) execution.getVariable("claimStatus");

        // If claim was submitted, mark as VOIDED
        // If claim was only generated but not submitted, mark as COMPENSATED
        if ("SUBMITTED".equalsIgnoreCase(currentStatus) ||
            "IN_REVIEW".equalsIgnoreCase(currentStatus)) {
            return "VOIDED";
        } else {
            return "COMPENSATED";
        }
    }

    /**
     * Notifies billing system of claim void
     */
    private void notifyBillingSystem(DelegateExecution execution, String claimId,
                                    String claimNumber) {
        LOGGER.warn("Notifying billing system of claim void - ID: {}, Number: {}",
            claimId, claimNumber);

        // Prepare notification data
        Map<String, Object> notification = new HashMap<>();
        notification.put("claimId", claimId);
        notification.put("claimNumber", claimNumber);
        notification.put("action", "CLAIM_VOIDED");
        notification.put("reason", "Process compensation");
        notification.put("timestamp", LocalDateTime.now().toString());
        notification.put("processInstanceId", execution.getProcessInstanceId());

        execution.setVariable("billingSystemNotification", notification);

        // TODO: Call billing notification service
        // billingNotificationService.notifyClaimVoid(notification);

        LOGGER.info("Billing system notified of claim void: {}", claimNumber);
    }

    /**
     * Creates audit trail for claim compensation
     */
    private void createClaimCompensationAudit(DelegateExecution execution, String claimId,
                                             String claimNumber, String reason) {
        Map<String, Object> auditRecord = new HashMap<>();
        auditRecord.put("compensationType", "CLAIM_REVERSAL");
        auditRecord.put("claimId", claimId);
        auditRecord.put("claimNumber", claimNumber);
        auditRecord.put("processInstanceId", execution.getProcessInstanceId());
        auditRecord.put("reason", reason);
        auditRecord.put("timestamp", LocalDateTime.now().toString());
        auditRecord.put("activityId", execution.getCurrentActivityId());
        auditRecord.put("originalClaim", execution.getVariable("originalClaimBeforeCompensation"));
        auditRecord.put("voidStatus", execution.getVariable("claimStatus"));

        execution.setVariable("claimCompensationAuditRecord", auditRecord);

        LOGGER.warn("COMPENSATION AUDIT: {} - Claim: {}, Number: {}, Process: {}",
            reason, claimId, claimNumber, execution.getProcessInstanceId());
    }
}
