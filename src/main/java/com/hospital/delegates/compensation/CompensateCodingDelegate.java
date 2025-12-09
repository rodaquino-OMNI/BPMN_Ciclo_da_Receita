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
 * Compensation Handler for Coding Reversal
 *
 * Reverts clinical coding assignments (ICD-10, TUSS, CBHPM, DRG) when process requires rollback.
 * Implements SAGA pattern compensation for SUB_02_Clinical_Coding process.
 */
@Component
@Named("compensateCodingDelegate")
public class CompensateCodingDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompensateCodingDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String processInstanceId = execution.getProcessInstanceId();

        LOGGER.warn("COMPENSATION TRIGGERED: Reversing coding assignments for process: {}", processInstanceId);

        try {
            // Check if compensation already performed (idempotency)
            Boolean compensationCompleted = (Boolean) execution.getVariable("codingCompensationCompleted");
            if (Boolean.TRUE.equals(compensationCompleted)) {
                LOGGER.info("COMPENSATION SKIPPED: Coding already compensated for process: {}", processInstanceId);
                return;
            }

            // Retrieve coding data before clearing
            String patientId = (String) execution.getVariable("patientId");
            String encounterCode = (String) execution.getVariable("encounterCode");
            String icd10Codes = (String) execution.getVariable("icd10Codes");
            String tussCodes = (String) execution.getVariable("tussCodes");
            String cbhpmCodes = (String) execution.getVariable("cbhpmCodes");
            String drgCode = (String) execution.getVariable("drgCode");

            LOGGER.warn("COMPENSATION DETAILS: Patient: {}, Encounter: {}, ICD-10: {}, DRG: {}",
                patientId, encounterCode, icd10Codes, drgCode);

            // Reverse code assignments
            reverseCodeAssignments(execution, patientId, encounterCode);

            // Clear DRG classification
            reverseDRGClassification(execution, drgCode);

            // Create audit trail
            createCodingCompensationAudit(execution, patientId, encounterCode,
                "Coding assignments reversed");

            // Mark compensation as completed
            execution.setVariable("codingCompensationCompleted", true);
            execution.setVariable("codingCompensationReason", "Clinical coding reverted due to process rollback");
            execution.setVariable("codingCompensationTimestamp", LocalDateTime.now().toString());

            LOGGER.warn("COMPENSATION COMPLETED: Coding cleared for patient {} in process: {}",
                patientId, processInstanceId);

        } catch (Exception e) {
            LOGGER.error("COMPENSATION FAILED for process {}: {}", processInstanceId, e.getMessage(), e);

            // Log failure but don't throw - compensation failures shouldn't block process
            execution.setVariable("codingCompensationCompleted", false);
            execution.setVariable("codingCompensationError", e.getMessage());

            // Create failure audit
            createCodingCompensationAudit(execution,
                (String) execution.getVariable("patientId"),
                (String) execution.getVariable("encounterCode"),
                "Compensation failed: " + e.getMessage());
        }
    }

    /**
     * Reverses all code assignments (ICD-10, TUSS, CBHPM)
     */
    private void reverseCodeAssignments(DelegateExecution execution, String patientId,
                                       String encounterCode) {
        LOGGER.warn("Clearing code assignments - Patient: {}, Encounter: {}", patientId, encounterCode);

        // Store original codes for audit before clearing
        Map<String, Object> originalCodes = new HashMap<>();
        originalCodes.put("icd10Codes", execution.getVariable("icd10Codes"));
        originalCodes.put("tussCodes", execution.getVariable("tussCodes"));
        originalCodes.put("cbhpmCodes", execution.getVariable("cbhpmCodes"));
        execution.setVariable("originalCodesBeforeCompensation", originalCodes);

        // Clear code assignments
        execution.removeVariable("icd10Codes");
        execution.removeVariable("tussCodes");
        execution.removeVariable("cbhpmCodes");
        execution.removeVariable("primaryDiagnosis");
        execution.removeVariable("secondaryDiagnoses");
        execution.removeVariable("procedureCodes");

        // Reset coding status
        execution.setVariable("codingStatus", "COMPENSATED");
        execution.setVariable("codingReversalDate", LocalDateTime.now().toString());

        // TODO: Call coding service to clear assignments
        // codingService.clearCodeAssignments(patientId, encounterCode);

        LOGGER.info("Code assignments cleared for encounter: {}", encounterCode);
    }

    /**
     * Reverses DRG classification
     */
    private void reverseDRGClassification(DelegateExecution execution, String drgCode) {
        LOGGER.warn("Clearing DRG classification - DRG: {}", drgCode);

        // Store original DRG for audit
        execution.setVariable("originalDRGBeforeCompensation", drgCode);

        // Clear DRG classification
        execution.removeVariable("drgCode");
        execution.removeVariable("drgDescription");
        execution.removeVariable("drgWeight");
        execution.removeVariable("drgSeverityLevel");

        // Reset DRG status
        execution.setVariable("drgStatus", "PENDING");

        // TODO: Call DRG service to clear classification
        // drgService.clearClassification(drgCode);

        LOGGER.info("DRG classification cleared: {}", drgCode);
    }

    /**
     * Creates audit trail for coding compensation
     */
    private void createCodingCompensationAudit(DelegateExecution execution, String patientId,
                                              String encounterCode, String reason) {
        Map<String, Object> auditRecord = new HashMap<>();
        auditRecord.put("compensationType", "CODING_REVERSAL");
        auditRecord.put("patientId", patientId);
        auditRecord.put("encounterCode", encounterCode);
        auditRecord.put("processInstanceId", execution.getProcessInstanceId());
        auditRecord.put("reason", reason);
        auditRecord.put("timestamp", LocalDateTime.now().toString());
        auditRecord.put("activityId", execution.getCurrentActivityId());
        auditRecord.put("originalCodes", execution.getVariable("originalCodesBeforeCompensation"));

        execution.setVariable("codingCompensationAuditRecord", auditRecord);

        LOGGER.warn("COMPENSATION AUDIT: {} - Patient: {}, Encounter: {}, Process: {}",
            reason, patientId, encounterCode, execution.getProcessInstanceId());
    }
}
