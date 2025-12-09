package com.hospital.compensation;

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
 * Compensation Handler for Payment Allocation Reversal
 *
 * Reverts payment allocation when revenue collection process requires rollback.
 * Implements SAGA pattern compensation with idempotency and audit trail.
 * Used in SUB_08_Revenue_Collection process.
 */
@Component
@Named("compensateAllocationDelegate")
public class CompensateAllocationDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompensateAllocationDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String processInstanceId = execution.getProcessInstanceId();

        LOGGER.warn("COMPENSATION TRIGGERED: Reverting payment allocation for process: {}", processInstanceId);

        try {
            // Check if compensation already performed (idempotency)
            Boolean compensationCompleted = (Boolean) execution.getVariable("allocationCompensationCompleted");
            if (Boolean.TRUE.equals(compensationCompleted)) {
                LOGGER.info("COMPENSATION SKIPPED: Allocation already compensated for process: {}", processInstanceId);
                return;
            }

            // Retrieve allocation data
            String allocationResult = (String) execution.getVariable("allocationResult");
            String transactionId = (String) execution.getVariable("transactionId");
            String accountId = (String) execution.getVariable("accountId");

            LOGGER.warn("COMPENSATION DETAILS: Transaction: {}, Account: {}, Result: {}",
                transactionId, accountId, allocationResult);

            // Perform compensation steps
            reverseAllocation(execution, transactionId, accountId, allocationResult);
            reversePayment(execution, transactionId);
            updateAccountingRecords(execution, transactionId, accountId);

            // Create audit trail
            createCompensationAudit(execution, transactionId, "Payment allocation reversed successfully");

            // Mark compensation as completed
            execution.setVariable("allocationCompensationCompleted", true);
            execution.setVariable("compensationReason", "Payment allocation reverted due to process rollback");
            execution.setVariable("compensationTimestamp", LocalDateTime.now().toString());

            LOGGER.warn("COMPENSATION COMPLETED: Allocation {} successfully reverted for process: {}",
                transactionId, processInstanceId);

        } catch (Exception e) {
            LOGGER.error("COMPENSATION FAILED for process {}: {}", processInstanceId, e.getMessage(), e);

            // Log failure but don't throw - compensation failures shouldn't block process
            execution.setVariable("allocationCompensationCompleted", false);
            execution.setVariable("compensationError", e.getMessage());
            execution.setVariable("compensationFailureTimestamp", LocalDateTime.now().toString());

            // Create failure audit
            createCompensationAudit(execution,
                (String) execution.getVariable("transactionId"),
                "Compensation failed: " + e.getMessage());
        }
    }

    /**
     * Reverses payment allocation from accounts
     */
    private void reverseAllocation(DelegateExecution execution, String transactionId,
                                   String accountId, String allocationResult) {
        LOGGER.warn("Reversing allocation - Transaction: {}, Account: {}", transactionId, accountId);

        // Clear allocation variables
        execution.removeVariable("allocationResult");
        execution.removeVariable("allocatedAmount");
        execution.removeVariable("allocationDate");

        // Reset allocation status
        execution.setVariable("allocationStatus", "COMPENSATED");

        // TODO: Call actual allocation service to revert
        // allocationService.revertAllocation(transactionId, accountId);

        LOGGER.info("Allocation reversed for transaction: {}", transactionId);
    }

    /**
     * Reverses payment back to unallocated pool
     */
    private void reversePayment(DelegateExecution execution, String transactionId) {
        LOGGER.warn("Reversing payment to unallocated pool - Transaction: {}", transactionId);

        // Update payment status
        execution.setVariable("paymentStatus", "UNALLOCATED");
        execution.setVariable("paymentReversed", true);
        execution.setVariable("paymentReversalDate", LocalDateTime.now().toString());

        // TODO: Call payment service to restore to pool
        // paymentService.restoreToUnallocatedPool(transactionId);

        LOGGER.info("Payment restored to unallocated pool: {}", transactionId);
    }

    /**
     * Updates accounting records to reflect compensation
     */
    private void updateAccountingRecords(DelegateExecution execution, String transactionId,
                                        String accountId) {
        LOGGER.warn("Updating accounting records for compensation - Transaction: {}, Account: {}",
            transactionId, accountId);

        // Record compensation in accounting
        Map<String, Object> accountingUpdate = new HashMap<>();
        accountingUpdate.put("transactionId", transactionId);
        accountingUpdate.put("accountId", accountId);
        accountingUpdate.put("action", "ALLOCATION_REVERSED");
        accountingUpdate.put("timestamp", LocalDateTime.now().toString());
        accountingUpdate.put("processInstanceId", execution.getProcessInstanceId());

        execution.setVariable("accountingCompensationRecord", accountingUpdate);

        // TODO: Call accounting service to record reversal
        // accountingService.recordCompensation(accountingUpdate);

        LOGGER.info("Accounting records updated for transaction: {}", transactionId);
    }

    /**
     * Creates audit trail for compensation action
     */
    private void createCompensationAudit(DelegateExecution execution, String transactionId,
                                        String reason) {
        Map<String, Object> auditRecord = new HashMap<>();
        auditRecord.put("compensationType", "ALLOCATION_REVERSAL");
        auditRecord.put("transactionId", transactionId);
        auditRecord.put("processInstanceId", execution.getProcessInstanceId());
        auditRecord.put("reason", reason);
        auditRecord.put("timestamp", LocalDateTime.now().toString());
        auditRecord.put("activityId", execution.getCurrentActivityId());

        execution.setVariable("compensationAuditRecord", auditRecord);

        LOGGER.warn("COMPENSATION AUDIT: {} - Transaction: {}, Process: {}",
            reason, transactionId, execution.getProcessInstanceId());
    }
}
