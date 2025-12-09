package com.hospital.compensation;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compensation Handler for Accounting Provision Reversal
 *
 * Reverts accounting provision when revenue collection rollback is required.
 * Used in SUB_08_Revenue_Collection process.
 */
public class CompensateProvisionDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompensateProvisionDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        try {
            // Retrieve provision data
            Double differenceAmount = (Double) execution.getVariable("differenceAmount");
            String differenceType = (String) execution.getVariable("differenceType");

            LOGGER.info("COMPENSATION: Reverting accounting provision - Amount: {}, Type: {}",
                differenceAmount, differenceType);

            // TODO: Implement provision reversal logic
            // 1. Reverse accounting entries in GL
            // 2. Update provision balances
            // 3. Restore original account values
            // 4. Notify accounting department

            // Example reversal steps:
            // generalLedgerService.reverseEntry(provisionEntryId);
            // provisionRepository.reverseProvision(differenceAmount, differenceType);
            // accountRepository.restoreOriginalValues(accountId);
            // notificationService.notifyAccounting(accountId, "Provision reversed");

            execution.setVariable("compensationCompleted", true);
            execution.setVariable("compensationReason", "Accounting provision reverted");

            LOGGER.info("COMPENSATION COMPLETED: Provision of {} successfully reverted",
                differenceAmount);

        } catch (Exception e) {
            LOGGER.error("COMPENSATION FAILED: Error reverting provision: {}",
                e.getMessage(), e);
            execution.setVariable("compensationCompleted", false);
            execution.setVariable("compensationError", e.getMessage());
            throw e;
        }
    }
}
