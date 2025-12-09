package com.hospital.delegates.medicalcoding;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * Delegate to assign ICD-10 and CPT codes to medical procedures
 */
public class AssignCodesDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssignCodesDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        LOGGER.info("Assigning medical codes for process instance: {}",
            execution.getProcessInstanceId());

        try {
            // Get input variables
            String procedureDescription = (String) execution.getVariable("procedureDescription");
            String diagnosisDescription = (String) execution.getVariable("diagnosisDescription");
            String medicalRecordId = (String) execution.getVariable("medicalRecordId");

            LOGGER.debug("Assigning codes - Procedure: {}, Diagnosis: {}, Record: {}",
                procedureDescription, diagnosisDescription, medicalRecordId);

            // Assign codes
            MedicalCodes codes = assignMedicalCodes(procedureDescription, diagnosisDescription);

            // Set output variables
            execution.setVariable("icd10Codes", codes.icd10Codes);
            execution.setVariable("cptCodes", codes.cptCodes);
            execution.setVariable("primaryDiagnosisCode", codes.primaryDiagnosisCode);
            execution.setVariable("primaryProcedureCode", codes.primaryProcedureCode);
            execution.setVariable("codingDate", codes.codingDate);
            execution.setVariable("requiresCodingReview", codes.requiresReview);

            LOGGER.info("Medical codes assigned - ICD-10: {}, CPT: {}, Requires review: {}",
                codes.icd10Codes, codes.cptCodes, codes.requiresReview);

        } catch (Exception e) {
            LOGGER.error("Error assigning medical codes: {}", e.getMessage(), e);
            execution.setVariable("codingError", e.getMessage());
            throw e;
        }
    }

    private MedicalCodes assignMedicalCodes(String procedureDesc, String diagnosisDesc) {
        // Simulated medical coding assignment
        // TODO: Replace with actual medical coding engine or NLP-based code assignment
        MedicalCodes codes = new MedicalCodes();

        // Example ICD-10 codes
        codes.icd10Codes = new ArrayList<>();
        codes.icd10Codes.add("I10");     // Essential hypertension (example)
        codes.icd10Codes.add("E11.9");   // Type 2 diabetes (example)
        codes.primaryDiagnosisCode = "I10";

        // Example CPT codes
        codes.cptCodes = new ArrayList<>();
        codes.cptCodes.add("99213");     // Office visit (example)
        codes.cptCodes.add("80053");     // Comprehensive metabolic panel (example)
        codes.primaryProcedureCode = "99213";

        codes.codingDate = java.time.LocalDateTime.now().toString();
        codes.requiresReview = false;

        return codes;
    }

    private static class MedicalCodes {
        List<String> icd10Codes;
        List<String> cptCodes;
        String primaryDiagnosisCode;
        String primaryProcedureCode;
        String codingDate;
        Boolean requiresReview;
    }
}
