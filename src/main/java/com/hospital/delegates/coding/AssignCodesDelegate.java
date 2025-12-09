package com.hospital.delegates.coding;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.hospital.delegates.coding.exceptions.CodingException;

import jakarta.inject.Named;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Production-grade delegate for AI/LLM-powered medical code assignment
 * Supports ICD-10 (Brazilian), TUSS, CBHPM, and DRG classification
 *
 * Features:
 * - AI/LLM code suggestion patterns (structure for future implementation)
 * - ICD-10 code assignment with confidence scoring
 * - TUSS/CBHPM procedure code mapping
 * - DRG classification engine
 * - Code combination validation
 * - CID x Procedure compatibility checks
 * - Auto-approve for confidence >95%
 * - DMN decision table integration
 * - Comprehensive audit trails
 * - BPMN Error event handling
 *
 * @author Revenue Cycle System
 * @version 2.0.0
 */
@Component
@Named("assignCodesDelegate")
public class AssignCodesDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssignCodesDelegate.class);
    private static final DateTimeFormatter AUDIT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    // Confidence thresholds
    private static final double AUTO_APPROVE_THRESHOLD = 0.95;
    private static final double MANUAL_REVIEW_THRESHOLD = 0.75;

    // Code validation patterns (Brazilian ICD-10)
    private static final Pattern ICD10_PATTERN = Pattern.compile("^[A-Z][0-9]{2}(\\.[0-9]{1,2})?$");
    private static final Pattern TUSS_PATTERN = Pattern.compile("^[0-9]{8}$");
    private static final Pattern CBHPM_PATTERN = Pattern.compile("^[0-9]{1,2}\\.[0-9]{2}\\.[0-9]{2}\\.[0-9]{2}[-][0-9]{1}$");

    // DRG classification ranges
    private static final Map<String, String> DRG_GROUPS = initDrgGroups();

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String processInstanceId = execution.getProcessInstanceId();
        String activityId = execution.getCurrentActivityId();

        LOGGER.info("[CODING] Starting code assignment - Process: {}, Activity: {}",
            processInstanceId, activityId);

        AuditTrail audit = new AuditTrail(processInstanceId, activityId);

        try {
            // 1. Extract input variables
            CodingInput input = extractInputVariables(execution);
            audit.addEntry("Input extraction completed", input.toString());

            // 2. Validate input data
            validateInput(input);
            audit.addEntry("Input validation passed", null);

            // 3. AI/LLM-powered code suggestion (pattern for future implementation)
            CodeSuggestions suggestions = suggestCodesWithAI(input);
            audit.addEntry("AI code suggestions generated",
                String.format("ICD-10: %d, TUSS: %d, Confidence: %.2f%%",
                    suggestions.icd10Codes.size(),
                    suggestions.tussCodes.size(),
                    suggestions.overallConfidence * 100));

            // 4. Validate code combinations
            validateCodeCombinations(suggestions);
            audit.addEntry("Code combination validation passed", null);

            // 5. Perform DRG classification
            DrgClassification drg = classifyDRG(suggestions);
            audit.addEntry("DRG classification completed", drg.toString());

            // 6. Calculate confidence and determine review requirement
            boolean requiresReview = suggestions.overallConfidence < AUTO_APPROVE_THRESHOLD;
            String reviewReason = determineReviewReason(suggestions);
            audit.addEntry("Confidence analysis completed",
                String.format("Auto-approve: %b, Reason: %s", !requiresReview, reviewReason));

            // 7. Integrate with DMN decision table for validation
            Map<String, Object> dmnResult = evaluateCodingDecision(execution, suggestions);
            audit.addEntry("DMN validation completed", dmnResult.toString());

            // 8. Set output variables
            setOutputVariables(execution, suggestions, drg, requiresReview, reviewReason, audit);

            // 9. Log final audit trail
            logAuditTrail(execution, audit);

            LOGGER.info("[CODING] Code assignment completed successfully - Process: {}, Auto-approved: {}",
                processInstanceId, !requiresReview);

        } catch (CodingException e) {
            audit.addEntry("ERROR: Coding exception occurred", e.toString());
            handleCodingError(execution, e, audit);
            throw new BpmnError("CODING_ERROR", e.getMessage());

        } catch (Exception e) {
            audit.addEntry("ERROR: Unexpected exception", e.getMessage());
            LOGGER.error("[CODING] Unexpected error in code assignment - Process: {}",
                processInstanceId, e);
            execution.setVariable("codingError", e.getMessage());
            execution.setVariable("codingErrorType", "SYSTEM_ERROR");
            logAuditTrail(execution, audit);
            throw new BpmnError("CODING_SYSTEM_ERROR", "System error during code assignment");
        }
    }

    /**
     * Extract and validate input variables from process execution
     */
    private CodingInput extractInputVariables(DelegateExecution execution) {
        CodingInput input = new CodingInput();

        input.medicalRecordId = (String) execution.getVariable("medicalRecordId");
        input.patientId = (String) execution.getVariable("patientId");
        input.procedureDescription = (String) execution.getVariable("procedureDescription");
        input.diagnosisDescription = (String) execution.getVariable("diagnosisDescription");
        input.clinicalNotes = (String) execution.getVariable("clinicalNotes");
        input.procedureDate = (String) execution.getVariable("procedureDate");
        input.providerSpecialty = (String) execution.getVariable("providerSpecialty");
        input.encounterType = (String) execution.getVariable("encounterType");

        @SuppressWarnings("unchecked")
        List<String> comorbidities = (List<String>) execution.getVariable("comorbidities");
        input.comorbidities = comorbidities != null ? comorbidities : new ArrayList<>();

        return input;
    }

    /**
     * Validate input data completeness and format
     */
    private void validateInput(CodingInput input) {
        List<String> errors = new ArrayList<>();

        if (input.medicalRecordId == null || input.medicalRecordId.trim().isEmpty()) {
            errors.add("Medical record ID is required");
        }
        if (input.procedureDescription == null || input.procedureDescription.trim().isEmpty()) {
            errors.add("Procedure description is required");
        }
        if (input.diagnosisDescription == null || input.diagnosisDescription.trim().isEmpty()) {
            errors.add("Diagnosis description is required");
        }

        if (!errors.isEmpty()) {
            throw new CodingException("Input validation failed: " + String.join("; ", errors),
                "INPUT_VALIDATION_ERROR", "GENERAL", null);
        }
    }

    /**
     * AI/LLM-powered code suggestion system
     * NOTE: This is a structural pattern - integrate with actual AI/LLM service
     *
     * Future integration points:
     * - OpenAI GPT-4 for medical coding
     * - AWS HealthLake for clinical NLP
     * - Google Healthcare API for medical entity recognition
     * - Custom trained models for Brazilian healthcare codes
     */
    private CodeSuggestions suggestCodesWithAI(CodingInput input) {
        CodeSuggestions suggestions = new CodeSuggestions();

        // Simulated AI/LLM code suggestion
        // TODO: Replace with actual AI/LLM integration
        // Example: response = aiService.suggestCodes(input.procedureDescription, input.diagnosisDescription)

        // ICD-10 code assignment based on diagnosis
        suggestions.icd10Codes = assignICD10Codes(input);

        // TUSS procedure code assignment
        suggestions.tussCodes = assignTUSSCodes(input);

        // CBHPM code mapping
        suggestions.cbhpmCodes = assignCBHPMCodes(input);

        // Calculate confidence scores for each code
        suggestions.icd10Confidence = calculateCodeConfidence(suggestions.icd10Codes, input.diagnosisDescription);
        suggestions.tussConfidence = calculateCodeConfidence(suggestions.tussCodes, input.procedureDescription);

        // Overall confidence weighted by code importance
        suggestions.overallConfidence = (suggestions.icd10Confidence * 0.6) + (suggestions.tussConfidence * 0.4);

        // Set primary codes
        suggestions.primaryDiagnosisCode = suggestions.icd10Codes.isEmpty() ? null : suggestions.icd10Codes.get(0).code;
        suggestions.primaryProcedureCode = suggestions.tussCodes.isEmpty() ? null : suggestions.tussCodes.get(0).code;

        suggestions.codingDate = LocalDateTime.now().format(AUDIT_FORMATTER);

        return suggestions;
    }

    /**
     * Assign ICD-10 codes with AI-assisted selection
     */
    private List<CodeWithConfidence> assignICD10Codes(CodingInput input) {
        List<CodeWithConfidence> codes = new ArrayList<>();

        // Primary diagnosis code (simulated AI selection)
        String diagnosisLower = input.diagnosisDescription.toLowerCase();

        // Example AI-powered mapping (replace with actual AI service)
        if (diagnosisLower.contains("hipertens") || diagnosisLower.contains("pressão alta")) {
            codes.add(new CodeWithConfidence("I10", "Hipertensão essencial (primária)", 0.95));
        } else if (diagnosisLower.contains("diabetes")) {
            codes.add(new CodeWithConfidence("E11.9", "Diabetes mellitus tipo 2 sem complicações", 0.92));
        } else if (diagnosisLower.contains("pneumonia")) {
            codes.add(new CodeWithConfidence("J18.9", "Pneumonia não especificada", 0.88));
        } else {
            // Default fallback code
            codes.add(new CodeWithConfidence("R69", "Causas desconhecidas e não especificadas de morbidade", 0.50));
        }

        // Add comorbidity codes
        for (String comorbidity : input.comorbidities) {
            codes.add(new CodeWithConfidence(mapComorbidityToICD10(comorbidity), comorbidity, 0.85));
        }

        return codes;
    }

    /**
     * Assign TUSS procedure codes
     */
    private List<CodeWithConfidence> assignTUSSCodes(CodingInput input) {
        List<CodeWithConfidence> codes = new ArrayList<>();

        String procedureLower = input.procedureDescription.toLowerCase();

        // Example TUSS code mapping (Brazilian standard)
        if (procedureLower.contains("consulta") || procedureLower.contains("avaliação")) {
            codes.add(new CodeWithConfidence("10101012", "Consulta médica em consultório", 0.93));
        } else if (procedureLower.contains("cirurgia") || procedureLower.contains("operação")) {
            codes.add(new CodeWithConfidence("31001017", "Cirurgia geral de pequeno porte", 0.87));
        } else if (procedureLower.contains("exame") || procedureLower.contains("análise")) {
            codes.add(new CodeWithConfidence("20101015", "Exames laboratoriais", 0.90));
        } else {
            codes.add(new CodeWithConfidence("10101012", "Procedimento ambulatorial", 0.70));
        }

        return codes;
    }

    /**
     * Assign CBHPM codes (Brazilian medical fee table)
     */
    private List<CodeWithConfidence> assignCBHPMCodes(CodingInput input) {
        List<CodeWithConfidence> codes = new ArrayList<>();

        // Map TUSS to CBHPM (simplified example)
        String procedureLower = input.procedureDescription.toLowerCase();

        if (procedureLower.contains("consulta")) {
            codes.add(new CodeWithConfidence("10.01.01.01-4", "Consulta médica", 0.95));
        } else if (procedureLower.contains("cirurgia")) {
            codes.add(new CodeWithConfidence("31.01.01.01-0", "Procedimento cirúrgico", 0.88));
        }

        return codes;
    }

    /**
     * Calculate confidence score for assigned codes
     */
    private double calculateCodeConfidence(List<CodeWithConfidence> codes, String description) {
        if (codes.isEmpty()) {
            return 0.0;
        }

        // Weighted average based on code positions
        double totalConfidence = 0.0;
        double totalWeight = 0.0;

        for (int i = 0; i < codes.size(); i++) {
            double weight = 1.0 / (i + 1); // Primary code has higher weight
            totalConfidence += codes.get(i).confidence * weight;
            totalWeight += weight;
        }

        return totalConfidence / totalWeight;
    }

    /**
     * Validate code combinations for compatibility
     */
    private void validateCodeCombinations(CodeSuggestions suggestions) {
        List<String> errors = new ArrayList<>();

        // Validate ICD-10 x TUSS pairing
        if (!suggestions.icd10Codes.isEmpty() && !suggestions.tussCodes.isEmpty()) {
            boolean compatible = isICD10TUSSCompatible(
                suggestions.icd10Codes.get(0).code,
                suggestions.tussCodes.get(0).code
            );

            if (!compatible) {
                errors.add(String.format("ICD-10 code %s incompatible with TUSS code %s",
                    suggestions.icd10Codes.get(0).code,
                    suggestions.tussCodes.get(0).code));
            }
        }

        // Validate code formats
        for (CodeWithConfidence code : suggestions.icd10Codes) {
            if (!ICD10_PATTERN.matcher(code.code).matches()) {
                errors.add("Invalid ICD-10 format: " + code.code);
            }
        }

        for (CodeWithConfidence code : suggestions.tussCodes) {
            if (!TUSS_PATTERN.matcher(code.code).matches()) {
                errors.add("Invalid TUSS format: " + code.code);
            }
        }

        if (!errors.isEmpty()) {
            throw new CodingException("Code combination validation failed",
                "CODE_COMBINATION_ERROR", "VALIDATION", String.join("; ", errors));
        }
    }

    /**
     * Check ICD-10 and TUSS code compatibility
     */
    private boolean isICD10TUSSCompatible(String icd10, String tuss) {
        // Simplified compatibility check
        // TODO: Implement comprehensive compatibility matrix

        // Example: Surgical TUSS codes require surgical ICD-10 codes
        if (tuss.startsWith("31") && icd10.startsWith("Z")) {
            return false; // Z codes are not surgical
        }

        return true;
    }

    /**
     * Classify DRG (Diagnosis Related Group)
     */
    private DrgClassification classifyDRG(CodeSuggestions suggestions) {
        DrgClassification drg = new DrgClassification();

        if (suggestions.primaryDiagnosisCode == null) {
            drg.drgCode = "999";
            drg.drgDescription = "Ungroupable";
            drg.severity = "LOW";
            drg.expectedLOS = 0;
            drg.relativeWeight = 0.0;
            return drg;
        }

        // Simplified DRG grouping (Brazilian adaptation)
        String prefix = suggestions.primaryDiagnosisCode.substring(0, 1);
        String drgGroup = DRG_GROUPS.getOrDefault(prefix, "999");

        drg.drgCode = drgGroup;
        drg.drgDescription = getDrgDescription(drgGroup);
        drg.severity = calculateSeverity(suggestions);
        drg.expectedLOS = calculateExpectedLOS(drgGroup, drg.severity);
        drg.relativeWeight = calculateRelativeWeight(drgGroup, drg.severity);

        return drg;
    }

    /**
     * Determine if manual review is required
     */
    private String determineReviewReason(CodeSuggestions suggestions) {
        if (suggestions.overallConfidence >= AUTO_APPROVE_THRESHOLD) {
            return "AUTO_APPROVED_HIGH_CONFIDENCE";
        } else if (suggestions.overallConfidence >= MANUAL_REVIEW_THRESHOLD) {
            return "REVIEW_MEDIUM_CONFIDENCE";
        } else {
            return "REVIEW_LOW_CONFIDENCE";
        }
    }

    /**
     * Evaluate coding decision using DMN decision table
     */
    private Map<String, Object> evaluateCodingDecision(DelegateExecution execution,
                                                       CodeSuggestions suggestions) {
        Map<String, Object> result = new HashMap<>();

        try {
            // Prepare DMN input variables
            Map<String, Object> variables = new HashMap<>();
            variables.put("cid10Valid", !suggestions.icd10Codes.isEmpty());
            variables.put("tussCodeValid", !suggestions.tussCodes.isEmpty());
            variables.put("codePairingValid", true); // Already validated
            variables.put("quantityAppropriate", true);
            variables.put("modifiersCorrect", true);
            variables.put("documentationMatch", suggestions.overallConfidence > 0.80);

            // Execute DMN decision table
            DmnDecisionTableResult dmnResult = execution.getProcessEngineServices()
                .getDecisionService()
                .evaluateDecisionTableByKey("validateCoding", variables);

            if (!dmnResult.isEmpty()) {
                Map<String, Object> firstResult = dmnResult.getSingleResult();
                result.put("validationResult", firstResult.get("validationResult"));
                result.put("errorType", firstResult.get("errorType"));
                result.put("severity", firstResult.get("severity"));
                result.put("actionRequired", firstResult.get("actionRequired"));
            }

        } catch (Exception e) {
            LOGGER.warn("[CODING] DMN evaluation failed, using fallback: {}", e.getMessage());
            result.put("validationResult", "WARNING");
            result.put("errorType", "DMN_EVALUATION_FAILED");
            result.put("severity", "WARNING");
            result.put("actionRequired", "MANUAL_REVIEW");
        }

        return result;
    }

    /**
     * Set output variables in process execution
     */
    private void setOutputVariables(DelegateExecution execution, CodeSuggestions suggestions,
                                    DrgClassification drg, boolean requiresReview,
                                    String reviewReason, AuditTrail audit) {
        // Code assignments
        execution.setVariable("icd10Codes",
            suggestions.icd10Codes.stream().map(c -> c.code).collect(Collectors.toList()));
        execution.setVariable("tussCodes",
            suggestions.tussCodes.stream().map(c -> c.code).collect(Collectors.toList()));
        execution.setVariable("cbhpmCodes",
            suggestions.cbhpmCodes.stream().map(c -> c.code).collect(Collectors.toList())); // FIX: Use cbhpmCodes not tussCodes

        // Primary codes
        execution.setVariable("primaryDiagnosisCode", suggestions.primaryDiagnosisCode);
        execution.setVariable("primaryProcedureCode", suggestions.primaryProcedureCode);

        // Confidence and review
        execution.setVariable("codingConfidence", suggestions.overallConfidence);
        execution.setVariable("requiresCodingReview", requiresReview);
        execution.setVariable("codingReviewReason", reviewReason);

        // DRG classification
        execution.setVariable("drgCode", drg.drgCode);
        execution.setVariable("drgDescription", drg.drgDescription);
        execution.setVariable("drgSeverity", drg.severity);
        execution.setVariable("expectedLOS", drg.expectedLOS);
        execution.setVariable("drgRelativeWeight", drg.relativeWeight);

        // Metadata
        execution.setVariable("codingDate", suggestions.codingDate);
        execution.setVariable("codingMethod", "AI_ASSISTED");
    }

    /**
     * Handle coding errors with BPMN error events
     */
    private void handleCodingError(DelegateExecution execution, CodingException e,
                                   AuditTrail audit) {
        LOGGER.error("[CODING] Coding error - Process: {}, Error: {}",
            execution.getProcessInstanceId(), e.toString());

        execution.setVariable("codingError", e.getMessage());
        execution.setVariable("codingErrorCode", e.getErrorCode());
        execution.setVariable("codingErrorType", e.getCodeType());
        execution.setVariable("failedCode", e.getFailedCode());

        logAuditTrail(execution, audit);
    }

    /**
     * Log comprehensive audit trail
     */
    private void logAuditTrail(DelegateExecution execution, AuditTrail audit) {
        execution.setVariable("codingAuditTrail", audit.getEntries());
        execution.setVariable("codingAuditTimestamp", audit.timestamp);

        LOGGER.debug("[CODING] Audit trail: {}", audit.toString());
    }

    // Helper methods

    private static Map<String, String> initDrgGroups() {
        Map<String, String> groups = new HashMap<>();
        groups.put("A", "001"); // Infectious diseases
        groups.put("C", "010"); // Neoplasms
        groups.put("E", "020"); // Endocrine/metabolic
        groups.put("I", "030"); // Circulatory
        groups.put("J", "040"); // Respiratory
        groups.put("K", "050"); // Digestive
        groups.put("M", "060"); // Musculoskeletal
        groups.put("N", "070"); // Genitourinary
        groups.put("S", "080"); // Injury/poisoning
        return groups;
    }

    private String mapComorbidityToICD10(String comorbidity) {
        String lower = comorbidity.toLowerCase();
        if (lower.contains("diabetes")) return "E11.9";
        if (lower.contains("hipertens")) return "I10";
        if (lower.contains("obesidade")) return "E66.9";
        return "Z99.9";
    }

    private String getDrgDescription(String drgCode) {
        switch (drgCode) {
            case "001": return "Doenças Infecciosas e Parasitárias";
            case "010": return "Neoplasias";
            case "020": return "Doenças Endócrinas e Metabólicas";
            case "030": return "Doenças do Aparelho Circulatório";
            case "040": return "Doenças do Aparelho Respiratório";
            case "050": return "Doenças do Aparelho Digestivo";
            default: return "Outros";
        }
    }

    private String calculateSeverity(CodeSuggestions suggestions) {
        int codeCount = suggestions.icd10Codes.size() + suggestions.comorbidities.size();
        if (codeCount >= 5) return "HIGH";
        if (codeCount >= 3) return "MEDIUM";
        return "LOW";
    }

    private int calculateExpectedLOS(String drgCode, String severity) {
        int baseLOS = Integer.parseInt(drgCode) % 10 + 1;
        switch (severity) {
            case "HIGH": return baseLOS * 2;
            case "MEDIUM": return (int) (baseLOS * 1.5);
            default: return baseLOS;
        }
    }

    private double calculateRelativeWeight(String drgCode, String severity) {
        double baseWeight = Integer.parseInt(drgCode) / 100.0;
        switch (severity) {
            case "HIGH": return baseWeight * 1.5;
            case "MEDIUM": return baseWeight * 1.2;
            default: return baseWeight;
        }
    }

    // Inner classes

    private static class CodingInput {
        String medicalRecordId;
        String patientId;
        String procedureDescription;
        String diagnosisDescription;
        String clinicalNotes;
        String procedureDate;
        String providerSpecialty;
        String encounterType;
        List<String> comorbidities;

        @Override
        public String toString() {
            return String.format("Record=%s, Patient=%s, Procedure=%s, Diagnosis=%s",
                medicalRecordId, patientId, procedureDescription, diagnosisDescription);
        }
    }

    private static class CodeSuggestions {
        List<CodeWithConfidence> icd10Codes = new ArrayList<>();
        List<CodeWithConfidence> tussCodes = new ArrayList<>();
        List<CodeWithConfidence> cbhpmCodes = new ArrayList<>();
        List<String> comorbidities = new ArrayList<>();
        String primaryDiagnosisCode;
        String primaryProcedureCode;
        double icd10Confidence;
        double tussConfidence;
        double overallConfidence;
        String codingDate;
    }

    private static class CodeWithConfidence {
        String code;
        String description;
        double confidence;

        CodeWithConfidence(String code, String description, double confidence) {
            this.code = code;
            this.description = description;
            this.confidence = confidence;
        }
    }

    private static class DrgClassification {
        String drgCode;
        String drgDescription;
        String severity;
        int expectedLOS;
        double relativeWeight;

        @Override
        public String toString() {
            return String.format("DRG=%s (%s), Severity=%s, LOS=%d, Weight=%.2f",
                drgCode, drgDescription, severity, expectedLOS, relativeWeight);
        }
    }

    private static class AuditTrail {
        String processInstanceId;
        String activityId;
        String timestamp;
        List<AuditEntry> entries = new ArrayList<>();

        AuditTrail(String processInstanceId, String activityId) {
            this.processInstanceId = processInstanceId;
            this.activityId = activityId;
            this.timestamp = LocalDateTime.now().format(AUDIT_FORMATTER);
        }

        void addEntry(String action, String details) {
            entries.add(new AuditEntry(action, details));
        }

        List<String> getEntries() {
            return entries.stream()
                .map(AuditEntry::toString)
                .collect(Collectors.toList());
        }

        @Override
        public String toString() {
            return String.format("AuditTrail[Process=%s, Activity=%s, Entries=%d]",
                processInstanceId, activityId, entries.size());
        }
    }

    private static class AuditEntry {
        String timestamp;
        String action;
        String details;

        AuditEntry(String action, String details) {
            this.timestamp = LocalDateTime.now().format(AUDIT_FORMATTER);
            this.action = action;
            this.details = details;
        }

        @Override
        public String toString() {
            return String.format("[%s] %s: %s", timestamp, action, details);
        }
    }
}
