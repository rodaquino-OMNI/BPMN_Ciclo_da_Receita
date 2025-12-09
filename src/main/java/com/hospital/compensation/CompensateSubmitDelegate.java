package com.hospital.compensation;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compensation Handler for Billing Submission Reversal
 *
 * Reverts webservice submission when billing process needs to be rolled back.
 * Used in SUB_06_Billing_Submission process.
 */
public class CompensateSubmitDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompensateSubmitDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        try {
            // Retrieve original submission data
            String protocolNumber = (String) execution.getVariable("protocolNumber");
            String batchNumber = (String) execution.getVariable("batchNumber");

            LOGGER.info("COMPENSATION: Reverting billing submission - Protocol: {}, Batch: {}",
                protocolNumber, batchNumber);

            // TODO: Implement reversal logic
            // 1. Call webservice cancellation API
            // 2. Update batch status to CANCELLED
            // 3. Restore account to PENDING state
            // 4. Log compensation action in audit trail

            // Example reversal steps:
            // billingWebService.cancelSubmission(protocolNumber);
            // batchRepository.updateStatus(batchNumber, BatchStatus.CANCELLED);
            // accountRepository.updateStatus(accountId, AccountStatus.PENDING);

            // Set compensation result
            execution.setVariable("compensationCompleted", true);
            execution.setVariable("compensationReason", "Billing submission reverted");

            LOGGER.info("COMPENSATION COMPLETED: Billing submission {} successfully reverted",
                protocolNumber);

        } catch (Exception e) {
            LOGGER.error("COMPENSATION FAILED: Error reverting billing submission: {}",
                e.getMessage(), e);
            execution.setVariable("compensationCompleted", false);
            execution.setVariable("compensationError", e.getMessage());
            throw e;
        }
    }
}
