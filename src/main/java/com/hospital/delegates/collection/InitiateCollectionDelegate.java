package com.hospital.delegates.collection;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.inject.Named;

/**
 * Delegate to initiate patient payment collection process.
 *
 * This delegate creates collection cases for outstanding patient balances,
 * determines priority levels, and schedules first contact dates.
 *
 * @author Hospital Revenue Cycle Team
 * @version 1.0.0
 */
@Component
@Named("initiateCollectionDelegate")
public class InitiateCollectionDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitiateCollectionDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        LOGGER.info("Initiating collection process for process instance: {}",
            execution.getProcessInstanceId());

        try {
            // Get input variables
            String patientId = (String) execution.getVariable("patientId");
            Double patientBalance = (Double) execution.getVariable("patientBalance");
            String accountNumber = (String) execution.getVariable("accountNumber");

            LOGGER.debug("Initiating collection - Patient: {}, Balance: {}, Account: {}",
                patientId, patientBalance, accountNumber);

            // Initiate collection
            CollectionCase collectionCase = initiateCollection(patientId, patientBalance, accountNumber);

            // Set output variables
            execution.setVariable("collectionCaseId", collectionCase.caseId);
            execution.setVariable("collectionStatus", collectionCase.status);
            execution.setVariable("collectionInitiationDate", collectionCase.initiationDate);
            execution.setVariable("collectionPriority", collectionCase.priority);
            execution.setVariable("paymentPlanEligible", collectionCase.paymentPlanEligible);
            execution.setVariable("firstContactDate", collectionCase.firstContactDate);

            LOGGER.info("Collection initiated - Case ID: {}, Status: {}, Priority: {}",
                collectionCase.caseId, collectionCase.status, collectionCase.priority);

        } catch (Exception e) {
            LOGGER.error("Error initiating collection: {}", e.getMessage(), e);
            execution.setVariable("collectionError", e.getMessage());
            throw e;
        }
    }

    private CollectionCase initiateCollection(String patientId, Double balance, String account) {
        // Simulated collection initiation
        // TODO: Replace with actual collection management system integration
        CollectionCase collectionCase = new CollectionCase();
        collectionCase.caseId = "COLL-" + System.currentTimeMillis();
        collectionCase.status = "INITIATED";
        collectionCase.initiationDate = java.time.LocalDateTime.now().toString();
        collectionCase.priority = determinePriority(balance);
        collectionCase.paymentPlanEligible = balance != null && balance < 10000.0;
        collectionCase.firstContactDate = java.time.LocalDate.now().plusDays(5).toString();
        return collectionCase;
    }

    private String determinePriority(Double balance) {
        if (balance == null) return "LOW";
        if (balance >= 10000.0) return "HIGH";
        if (balance >= 2000.0) return "MEDIUM";
        return "LOW";
    }

    private static class CollectionCase {
        String caseId;
        String status;
        String initiationDate;
        String priority;
        Boolean paymentPlanEligible;
        String firstContactDate;
    }
}
