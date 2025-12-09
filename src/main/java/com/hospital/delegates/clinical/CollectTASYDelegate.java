package com.hospital.delegates.clinical;

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
 * Delegate to collect clinical data from TASY system
 * TASY is a hospital management system used in Brazilian healthcare
 */
@Component
@Named("collectTASYDelegate")
public class CollectTASYDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectTASYDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String processInstanceId = execution.getProcessInstanceId();
        LOGGER.info("Collecting TASY clinical data for process instance: {}", processInstanceId);

        try {
            // Get input variables
            String patientId = (String) execution.getVariable("patientId");
            String encounterType = (String) execution.getVariable("encounterType");

            LOGGER.debug("Collecting TASY data - Patient: {}, Encounter: {}", patientId, encounterType);

            // Simulate TASY data collection
            // TODO: Replace with actual TASY system integration
            Map<String, Object> clinicalData = collectClinicalData(patientId, encounterType);

            // Set output variables
            execution.setVariable("tasyDataCollected", true);
            execution.setVariable("clinicalData", clinicalData);
            execution.setVariable("tasyCollectionTimestamp", LocalDateTime.now().toString());

            LOGGER.info("TASY data collected successfully - Patient: {}, Records: {}",
                patientId, clinicalData.size());

        } catch (Exception e) {
            LOGGER.error("Error collecting TASY clinical data: {}", e.getMessage(), e);
            execution.setVariable("tasyDataCollected", false);
            execution.setVariable("tasyCollectionError", e.getMessage());
            throw e;
        }
    }

    /**
     * Simulate clinical data collection from TASY system
     */
    private Map<String, Object> collectClinicalData(String patientId, String encounterType) {
        // Simulated TASY data
        // TODO: Replace with actual TASY API integration
        Map<String, Object> data = new HashMap<>();
        data.put("patientId", patientId);
        data.put("encounterType", encounterType);
        data.put("vitalSigns", Map.of(
            "bloodPressure", "120/80",
            "temperature", "36.5",
            "heartRate", "72"
        ));
        data.put("diagnoses", java.util.List.of("R50.9", "Z00.00"));
        data.put("procedures", java.util.List.of("99213"));
        data.put("collectionDate", LocalDateTime.now().toString());

        return data;
    }
}
