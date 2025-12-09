package com.hospital.delegates.glosa;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Production-grade delegate to identify glosas (claim denials/rejections) from multiple sources:
 * - RPA portal scraping for denial notifications
 * - EDI file-based denial import (835/999 transaction sets)
 * - Remittance advice parsing
 *
 * Integrates with glosa-classification.dmn for intelligent denial categorization
 * and severity scoring based on ANS regulations and business rules.
 *
 * Features:
 * - Multi-source denial capture (RPA, EDI, manual)
 * - Pattern-based denial code extraction
 * - DMN-powered classification
 * - Severity and recoverability scoring
 * - ANS deadline calculation
 * - LLM integration readiness for complex denial analysis
 *
 * @author Revenue Cycle Team
 * @version 2.0
 */
public class IdentifyGlosaDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentifyGlosaDelegate.class);

    // ANS regulation deadlines for appeals (in days)
    private static final int ANS_ADMINISTRATIVE_APPEAL_DEADLINE = 10;
    private static final int ANS_CLINICAL_APPEAL_DEADLINE = 20;
    private static final int ANS_HIGH_VALUE_APPEAL_DEADLINE = 25;
    private static final int ANS_CONTRACTUAL_APPEAL_DEADLINE = 30;

    // Denial code patterns (CARC - Claim Adjustment Reason Codes)
    private static final Map<String, String> DENIAL_CODE_PATTERNS = new HashMap<String, String>() {{
        put("^1$", "DEDUCTIBLE_AMOUNT"); // Patient responsibility
        put("^2$", "COINSURANCE_AMOUNT");
        put("^3$", "COPAYMENT_AMOUNT");
        put("^4$", "PROCEDURE_INCONSISTENT_WITH_RULES");
        put("^5$", "PROCEDURE_INCONSISTENT_WITH_PROVIDER");
        put("^6$", "PRIOR_AUTHORIZATION_REQUIRED");
        put("^7$", "SERVICE_INCONSISTENT_WITH_PROVIDER_TYPE");
        put("^11$", "DIAGNOSIS_INCONSISTENT_WITH_PROCEDURE");
        put("^16$", "MISSING_INFORMATION");
        put("^18$", "DUPLICATE_CLAIM");
        put("^22$", "SERVICE_NOT_COMPATIBLE_WITH_DATE");
        put("^26$", "AUTHORIZATION_EXPIRED");
        put("^27$", "AUTHORIZATION_INSUFFICIENT");
        put("^29$", "TIME_LIMIT_FILING");
        put("^31$", "PATIENT_NOT_ELIGIBLE");
        put("^50$", "NON_COVERED_SERVICE");
        put("^96$", "NON_COVERED_CHARGE");
        put("^97$", "PAYMENT_ADJUSTED_CONTRACTUAL");
        put("^109$", "NOT_COVERED_MISSING_DOCUMENTATION");
        put("^119$", "BENEFIT_MAXIMUM_REACHED");
        put("^167$", "NOT_REASONABLE_NECESSARY");
        put("^197$", "PRECERTIFICATION_PENALTY");
        put("^204$", "SERVICE_NOT_MEDICALLY_NECESSARY");
        put("^252$", "ADMINISTRATIVE_SURCHARGE");
    }};

    // EDI 835 denial reason code groups
    private static final Set<String> ADMINISTRATIVE_CODES = new HashSet<>(Arrays.asList(
        "16", "18", "22", "29", "252"
    ));

    private static final Set<String> CLINICAL_CODES = new HashSet<>(Arrays.asList(
        "4", "11", "50", "96", "109", "167", "204"
    ));

    private static final Set<String> AUTHORIZATION_CODES = new HashSet<>(Arrays.asList(
        "6", "26", "27", "197"
    ));

    private static final Set<String> CONTRACTUAL_CODES = new HashSet<>(Arrays.asList(
        "31", "97", "119"
    ));

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        LOGGER.info("🔍 Identifying glosas for process instance: {} - Claim: {}",
            execution.getProcessInstanceId(),
            execution.getVariable("claimId"));

        try {
            // Extract input variables
            String claimId = (String) execution.getVariable("claimId");
            String denialSource = getStringVariable(execution, "denialSource", "REMITTANCE");
            String remittanceAdvice = (String) execution.getVariable("remittanceAdvice");
            String ediFilePath = (String) execution.getVariable("ediFilePath");
            String rpaPortalData = (String) execution.getVariable("rpaPortalData");
            Double claimAmount = getDoubleVariable(execution, "claimAmount", 0.0);

            LOGGER.debug("📋 Denial identification config - Source: {}, Claim: {}, Amount: {}",
                denialSource, claimId, claimAmount);

            // Multi-source denial capture
            GlosaIdentificationResult result = new GlosaIdentificationResult();
            result.claimId = claimId;
            result.identificationDate = LocalDateTime.now();

            switch (denialSource.toUpperCase()) {
                case "EDI":
                    result = identifyFromEDI(ediFilePath, claimId, claimAmount);
                    break;
                case "RPA":
                    result = identifyFromRPA(rpaPortalData, claimId, claimAmount);
                    break;
                case "REMITTANCE":
                default:
                    result = identifyFromRemittance(remittanceAdvice, claimId, claimAmount);
                    break;
            }

            // DMN classification for each denial
            if (result.hasGlosa && !result.denials.isEmpty()) {
                classifyDenialsWithDMN(execution, result);
            }

            // Calculate ANS compliance deadlines
            calculateANSDeadlines(result);

            // Prepare LLM integration data for complex denials
            prepareLLMAnalysisData(result);

            // Set comprehensive output variables
            setExecutionVariables(execution, result);

            LOGGER.info("✅ Glosa identification completed - Has glosa: {}, Count: {}, Total amount: ${}, Priority: {}",
                result.hasGlosa, result.denialCount, result.totalGlosaAmount, result.overallPriority);

        } catch (GlosaIdentificationException e) {
            LOGGER.error("❌ Glosa identification error: {}", e.getMessage(), e);
            execution.setVariable("glosaIdentificationError", e.getMessage());
            execution.setVariable("glosaIdentificationFailed", true);
            throw e;
        } catch (Exception e) {
            LOGGER.error("❌ Unexpected error identifying glosas: {}", e.getMessage(), e);
            execution.setVariable("glosaIdentificationError", e.getMessage());
            execution.setVariable("glosaIdentificationFailed", true);
            throw new GlosaIdentificationException("Failed to identify glosas", e);
        }
    }

    /**
     * Identify denials from EDI 835 remittance advice file
     */
    private GlosaIdentificationResult identifyFromEDI(String ediFilePath, String claimId, Double claimAmount)
            throws GlosaIdentificationException {
        LOGGER.debug("📄 Parsing EDI file: {}", ediFilePath);

        GlosaIdentificationResult result = new GlosaIdentificationResult();
        result.claimId = claimId;
        result.identificationDate = LocalDateTime.now();
        result.denialSource = "EDI_835";

        if (ediFilePath == null || ediFilePath.isEmpty()) {
            LOGGER.warn("⚠️ No EDI file path provided, skipping EDI parsing");
            return result;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(new File(ediFilePath)))) {
            String line;
            boolean inClaimSection = false;

            while ((line = reader.readLine()) != null) {
                // Parse EDI segments (simplified - production should use proper EDI parser)
                if (line.startsWith("CLP")) { // Claim Payment Information
                    String[] segments = line.split("\\*");
                    if (segments.length > 1 && segments[1].equals(claimId)) {
                        inClaimSection = true;
                        // CLP*claimId*status*charged*paid*patient_responsibility
                        if (segments.length > 4) {
                            Double paidAmount = Double.parseDouble(segments[4]);
                            result.totalGlosaAmount = claimAmount - paidAmount;
                        }
                    } else {
                        inClaimSection = false;
                    }
                }

                if (inClaimSection && line.startsWith("CAS")) { // Claim Adjustment
                    parseCASSegment(line, result);
                }
            }

            result.hasGlosa = !result.denials.isEmpty();
            result.denialCount = result.denials.size();

        } catch (IOException e) {
            throw new GlosaIdentificationException("Failed to parse EDI file: " + ediFilePath, e);
        } catch (NumberFormatException e) {
            throw new GlosaIdentificationException("Invalid EDI format in file: " + ediFilePath, e);
        }

        return result;
    }

    /**
     * Parse CAS (Claim Adjustment Segment) from EDI 835
     * Format: CAS*adjustment_group*reason_code*amount*quantity
     */
    private void parseCASSegment(String casSegment, GlosaIdentificationResult result) {
        String[] segments = casSegment.split("\\*");
        if (segments.length >= 4) {
            String adjustmentGroup = segments[1]; // CO (Contractual), PR (Patient), OA (Other)
            String reasonCode = segments[2];
            Double adjustmentAmount = Double.parseDouble(segments[3]);

            DenialItem denial = new DenialItem();
            denial.denialCode = reasonCode;
            denial.denialAmount = adjustmentAmount;
            denial.denialReason = DENIAL_CODE_PATTERNS.getOrDefault(reasonCode, "UNKNOWN_CODE");
            denial.adjustmentGroup = adjustmentGroup;

            // Categorize based on reason code
            if (ADMINISTRATIVE_CODES.contains(reasonCode)) {
                denial.denialCategory = "ADMINISTRATIVE";
            } else if (CLINICAL_CODES.contains(reasonCode)) {
                denial.denialCategory = "CLINICAL";
            } else if (AUTHORIZATION_CODES.contains(reasonCode)) {
                denial.denialCategory = "AUTHORIZATION";
            } else if (CONTRACTUAL_CODES.contains(reasonCode)) {
                denial.denialCategory = "CONTRACTUAL";
            } else {
                denial.denialCategory = "OTHER";
            }

            result.denials.add(denial);
        }
    }

    /**
     * Identify denials from RPA portal scraping data
     */
    private GlosaIdentificationResult identifyFromRPA(String rpaPortalData, String claimId, Double claimAmount) {
        LOGGER.debug("🤖 Processing RPA portal data for claim: {}", claimId);

        GlosaIdentificationResult result = new GlosaIdentificationResult();
        result.claimId = claimId;
        result.identificationDate = LocalDateTime.now();
        result.denialSource = "RPA_PORTAL";

        if (rpaPortalData == null || rpaPortalData.isEmpty()) {
            LOGGER.warn("⚠️ No RPA portal data provided");
            return result;
        }

        // Parse JSON-like RPA data structure (simplified)
        // Production implementation should use proper JSON parsing
        Pattern denialPattern = Pattern.compile("DENIAL:\\s*([A-Z_]+)\\s*\\|\\s*AMOUNT:\\s*(\\d+\\.?\\d*)\\s*\\|\\s*REASON:\\s*(.+?)(?:\\||$)");
        Matcher matcher = denialPattern.matcher(rpaPortalData);

        while (matcher.find()) {
            DenialItem denial = new DenialItem();
            denial.denialCode = matcher.group(1);
            denial.denialAmount = Double.parseDouble(matcher.group(2));
            denial.denialReason = matcher.group(3).trim();
            denial.denialCategory = categorizeDenialFromText(denial.denialReason);
            denial.adjustmentGroup = "RPA";

            result.denials.add(denial);
            result.totalGlosaAmount += denial.denialAmount;
        }

        result.hasGlosa = !result.denials.isEmpty();
        result.denialCount = result.denials.size();

        return result;
    }

    /**
     * Identify denials from remittance advice text
     */
    private GlosaIdentificationResult identifyFromRemittance(String remittance, String claimId, Double claimAmount) {
        LOGGER.debug("📝 Parsing remittance advice for claim: {}", claimId);

        GlosaIdentificationResult result = new GlosaIdentificationResult();
        result.claimId = claimId;
        result.identificationDate = LocalDateTime.now();
        result.denialSource = "REMITTANCE";

        if (remittance == null || remittance.isEmpty()) {
            LOGGER.warn("⚠️ No remittance advice provided");
            return result;
        }

        // Pattern matching for common denial indicators
        String[] denialKeywords = {
            "denied", "rejected", "not covered", "insufficient documentation",
            "prior authorization", "medical necessity", "duplicate", "untimely filing"
        };

        for (String keyword : denialKeywords) {
            if (remittance.toLowerCase().contains(keyword)) {
                result.hasGlosa = true;

                DenialItem denial = new DenialItem();
                denial.denialReason = extractDenialReason(remittance, keyword);
                denial.denialCategory = categorizeDenialFromText(denial.denialReason);
                denial.denialCode = "MANUAL_REVIEW";
                denial.denialAmount = claimAmount; // Default to full claim amount
                denial.adjustmentGroup = "MANUAL";
                denial.requiresLLMAnalysis = true; // Flag for LLM processing

                result.denials.add(denial);
                result.totalGlosaAmount = claimAmount;
                break; // For simplicity, capture first denial found
            }
        }

        result.denialCount = result.denials.size();

        return result;
    }

    /**
     * Extract denial reason from remittance text around keyword
     */
    private String extractDenialReason(String text, String keyword) {
        int index = text.toLowerCase().indexOf(keyword.toLowerCase());
        if (index == -1) return keyword;

        int start = Math.max(0, index - 50);
        int end = Math.min(text.length(), index + keyword.length() + 100);

        return text.substring(start, end).trim();
    }

    /**
     * Categorize denial based on text analysis
     */
    private String categorizeDenialFromText(String text) {
        String lower = text.toLowerCase();

        if (lower.contains("document") || lower.contains("form") || lower.contains("paper")) {
            return "ADMINISTRATIVE";
        } else if (lower.contains("medical") || lower.contains("clinical") || lower.contains("necessity")) {
            return "CLINICAL";
        } else if (lower.contains("authorization") || lower.contains("approval")) {
            return "AUTHORIZATION";
        } else if (lower.contains("contract") || lower.contains("coverage") || lower.contains("plan")) {
            return "CONTRACTUAL";
        }

        return "UNKNOWN";
    }

    /**
     * Classify denials using DMN decision table
     */
    private void classifyDenialsWithDMN(DelegateExecution execution, GlosaIdentificationResult result) {
        LOGGER.debug("🧠 Classifying {} denials with DMN engine", result.denials.size());

        try {
            ProcessEngine processEngine = execution.getProcessEngine();

            for (DenialItem denial : result.denials) {
                VariableMap variables = Variables.createVariables()
                    .putValue("glosaReason", denial.denialCode)
                    .putValue("glosaAmount", denial.denialAmount)
                    .putValue("documentationAvailable", true) // Default, should be checked
                    .putValue("previousAppeals", 0) // Should query history
                    .putValue("clinicalJustification", "ADEQUATE"); // Should assess

                DmnDecisionTableResult dmnResult = processEngine.getDecisionService()
                    .evaluateDecisionTableByKey("classifyGlosa", variables);

                if (!dmnResult.isEmpty()) {
                    Map<String, Object> dmnOutput = dmnResult.getSingleResult();

                    denial.glosaType = (String) dmnOutput.get("glosaType");
                    denial.recoverability = (String) dmnOutput.get("recoverability");
                    denial.recommendedAction = (String) dmnOutput.get("recommendedAction");
                    denial.priority = (String) dmnOutput.get("priority");
                    denial.slaDays = ((Number) dmnOutput.get("slaDays")).intValue();

                    LOGGER.debug("  ✓ Denial classified: {} - {} - {} (SLA: {} days)",
                        denial.glosaType, denial.recoverability, denial.recommendedAction, denial.slaDays);
                }
            }

            // Determine overall priority (highest priority wins)
            result.overallPriority = determineOverallPriority(result.denials);

        } catch (Exception e) {
            LOGGER.error("❌ DMN classification failed: {}", e.getMessage(), e);
            // Continue processing without DMN classification
        }
    }

    /**
     * Determine overall priority from multiple denials
     */
    private String determineOverallPriority(List<DenialItem> denials) {
        boolean hasCritical = denials.stream().anyMatch(d -> "CRITICAL".equals(d.priority));
        boolean hasHigh = denials.stream().anyMatch(d -> "HIGH".equals(d.priority));
        boolean hasMedium = denials.stream().anyMatch(d -> "MEDIUM".equals(d.priority));

        if (hasCritical) return "CRITICAL";
        if (hasHigh) return "HIGH";
        if (hasMedium) return "MEDIUM";
        return "LOW";
    }

    /**
     * Calculate ANS regulatory deadlines for appeals
     */
    private void calculateANSDeadlines(GlosaIdentificationResult result) {
        LocalDateTime now = LocalDateTime.now();

        for (DenialItem denial : result.denials) {
            int deadlineDays;

            switch (denial.denialCategory) {
                case "ADMINISTRATIVE":
                    deadlineDays = ANS_ADMINISTRATIVE_APPEAL_DEADLINE;
                    break;
                case "CLINICAL":
                    deadlineDays = ANS_CLINICAL_APPEAL_DEADLINE;
                    break;
                case "CONTRACTUAL":
                    deadlineDays = ANS_CONTRACTUAL_APPEAL_DEADLINE;
                    break;
                default:
                    deadlineDays = denial.slaDays > 0 ? denial.slaDays : ANS_CLINICAL_APPEAL_DEADLINE;
            }

            // High value claims get extended deadline
            if (denial.denialAmount > 20000) {
                deadlineDays = Math.max(deadlineDays, ANS_HIGH_VALUE_APPEAL_DEADLINE);
            }

            denial.ansDeadline = now.plusDays(deadlineDays);
            denial.ansDeadlineDays = deadlineDays;
        }
    }

    /**
     * Prepare data structure for LLM analysis of complex denials
     */
    private void prepareLLMAnalysisData(GlosaIdentificationResult result) {
        Map<String, Object> llmData = new HashMap<>();

        List<DenialItem> complexDenials = result.denials.stream()
            .filter(d -> d.requiresLLMAnalysis ||
                        "CLINICAL".equals(d.denialCategory) ||
                        d.denialAmount > 10000)
            .collect(java.util.stream.Collectors.toList());

        if (!complexDenials.isEmpty()) {
            llmData.put("denials", complexDenials);
            llmData.put("claimId", result.claimId);
            llmData.put("totalAmount", result.totalGlosaAmount);
            llmData.put("analysisType", "DENIAL_ROOT_CAUSE");
            llmData.put("timestamp", LocalDateTime.now().toString());

            result.llmAnalysisRequired = true;
            result.llmAnalysisData = llmData;
        }
    }

    /**
     * Set all execution variables for downstream processes
     */
    private void setExecutionVariables(DelegateExecution execution, GlosaIdentificationResult result) {
        execution.setVariable("hasGlosa", result.hasGlosa);
        execution.setVariable("glosaCount", result.denialCount);
        execution.setVariable("glosaAmount", result.totalGlosaAmount);
        execution.setVariable("glosaSource", result.denialSource);
        execution.setVariable("glosaIdentificationDate", result.identificationDate.toString());
        execution.setVariable("glosaOverallPriority", result.overallPriority);
        execution.setVariable("glosaLLMRequired", result.llmAnalysisRequired);

        // Serialize denials as JSON for process variables
        List<Map<String, Object>> denialsList = new ArrayList<>();
        for (DenialItem denial : result.denials) {
            Map<String, Object> denialMap = new HashMap<>();
            denialMap.put("code", denial.denialCode);
            denialMap.put("reason", denial.denialReason);
            denialMap.put("amount", denial.denialAmount);
            denialMap.put("category", denial.denialCategory);
            denialMap.put("type", denial.glosaType);
            denialMap.put("recoverability", denial.recoverability);
            denialMap.put("action", denial.recommendedAction);
            denialMap.put("priority", denial.priority);
            denialMap.put("ansDeadline", denial.ansDeadline != null ? denial.ansDeadline.toString() : null);
            denialMap.put("ansDeadlineDays", denial.ansDeadlineDays);
            denialsList.add(denialMap);
        }
        execution.setVariable("glosaDenials", denialsList);

        if (result.llmAnalysisRequired) {
            execution.setVariable("glosaLLMAnalysisData", result.llmAnalysisData);
        }
    }

    // Helper methods
    private String getStringVariable(DelegateExecution execution, String name, String defaultValue) {
        Object value = execution.getVariable(name);
        return value != null ? value.toString() : defaultValue;
    }

    private Double getDoubleVariable(DelegateExecution execution, String name, Double defaultValue) {
        Object value = execution.getVariable(name);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }

    /**
     * Container for glosa identification results
     */
    private static class GlosaIdentificationResult {
        String claimId;
        LocalDateTime identificationDate;
        String denialSource;
        boolean hasGlosa = false;
        int denialCount = 0;
        Double totalGlosaAmount = 0.0;
        String overallPriority = "LOW";
        List<DenialItem> denials = new ArrayList<>();
        boolean llmAnalysisRequired = false;
        Map<String, Object> llmAnalysisData;
    }

    /**
     * Individual denial item details
     */
    private static class DenialItem {
        String denialCode;
        String denialReason;
        Double denialAmount;
        String denialCategory;
        String adjustmentGroup;
        String glosaType;
        String recoverability;
        String recommendedAction;
        String priority;
        int slaDays;
        LocalDateTime ansDeadline;
        int ansDeadlineDays;
        boolean requiresLLMAnalysis = false;
    }

    /**
     * Custom exception for glosa identification errors
     */
    public static class GlosaIdentificationException extends Exception {
        public GlosaIdentificationException(String message) {
            super(message);
        }

        public GlosaIdentificationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
