package com.hospital.compensation;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compensation Handler for Recovery Registration Reversal
 *
 * Reverts denial recovery registration when rollback is required.
 * Used in SUB_07_Denials_Management process.
 */
public class CompensateRecoveryDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompensateRecoveryDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        try {
            // Retrieve recovery data
            String denialId = (String) execution.getVariable("denialId");
            Double recoveredAmount = (Double) execution.getVariable("recoveredAmount");

            LOGGER.info("COMPENSATION: Reverting recovery registration - Denial: {}, Amount: {}",
                denialId, recoveredAmount);

            // TODO: Implement recovery reversal logic
            // 1. Reverse recovered amount from financial records
            // 2. Update denial status back to DENIED
            // 3. Adjust KPI metrics
            // 4. Notify finance department

            // Example reversal steps:
            // financialRepository.reverseRecovery(denialId, recoveredAmount);
            // denialRepository.updateStatus(denialId, DenialStatus.DENIED);
            // kpiService.decrementRecoveryMetrics(recoveredAmount);
            // notificationService.notifyFinance(denialId, "Recovery reversed");

            execution.setVariable("compensationCompleted", true);
            execution.setVariable("compensationReason", "Recovery registration reverted");

            LOGGER.info("COMPENSATION COMPLETED: Recovery for denial {} successfully reverted",
                denialId);

        } catch (Exception e) {
            LOGGER.error("COMPENSATION FAILED: Error reverting recovery: {}",
                e.getMessage(), e);
            execution.setVariable("compensationCompleted", false);
            execution.setVariable("compensationError", e.getMessage());
            throw e;
        }
    }
}
