package com.hospital.delegates.eligibility;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.inject.Named;

/**
 * Delegate to check insurance coverage details for procedures.
 *
 * This delegate calculates coverage percentages, patient responsibility,
 * and pre-authorization requirements based on insurance plan rules.
 *
 * @author Hospital Revenue Cycle Team
 * @version 1.0.0
 */
@Component
@Named("checkCoverageDelegate")
public class CheckCoverageDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckCoverageDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        LOGGER.info("Checking coverage for process instance: {}",
            execution.getProcessInstanceId());

        try {
            // Get input variables
            String procedureCode = (String) execution.getVariable("procedureCode");
            String insurancePlan = (String) execution.getVariable("insurancePlan");
            Double procedureCost = (Double) execution.getVariable("procedureCost");

            LOGGER.debug("Checking coverage - Procedure: {}, Plan: {}, Cost: {}",
                procedureCode, insurancePlan, procedureCost);

            // Calculate coverage
            CoverageResult coverage = calculateCoverage(procedureCode, insurancePlan, procedureCost);

            // Set output variables
            execution.setVariable("coveragePercentage", coverage.coveragePercentage);
            execution.setVariable("coveredAmount", coverage.coveredAmount);
            execution.setVariable("patientResponsibility", coverage.patientResponsibility);
            execution.setVariable("requiresPreAuth", coverage.requiresPreAuth);

            LOGGER.info("Coverage check completed - Coverage: {}%, Patient responsibility: {}",
                coverage.coveragePercentage, coverage.patientResponsibility);

        } catch (Exception e) {
            LOGGER.error("Error checking coverage: {}", e.getMessage(), e);
            execution.setVariable("coverageError", e.getMessage());
            throw e;
        }
    }

    private CoverageResult calculateCoverage(String procedureCode, String plan, Double cost) {
        // Simulated coverage calculation
        // TODO: Replace with actual insurance plan rules engine
        CoverageResult result = new CoverageResult();
        result.coveragePercentage = 80.0;
        result.coveredAmount = cost != null ? cost * 0.8 : 0.0;
        result.patientResponsibility = cost != null ? cost * 0.2 : 0.0;
        result.requiresPreAuth = procedureCode != null && procedureCode.startsWith("SURG");
        return result;
    }

    private static class CoverageResult {
        Double coveragePercentage;
        Double coveredAmount;
        Double patientResponsibility;
        Boolean requiresPreAuth;
    }
}
