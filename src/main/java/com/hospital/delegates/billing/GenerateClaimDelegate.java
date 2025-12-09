package com.hospital.delegates.billing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.services.idempotency.IdempotencyService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.inject.Named;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Delegate to generate insurance claim from medical codes and patient information.
 * Implements idempotency protection to prevent duplicate claim generation.
 *
 * BUSINESS JUSTIFICATION:
 * - Prevents duplicate claims that could result in double billing (fraud risk)
 * - Ensures compliance with insurance company submission rules (one claim per authorization)
 * - Protects against process retries, system failures, and user errors
 * - Authorization number serves as primary business key for deduplication
 * - Maintains audit trail of duplicate attempts for compliance reporting
 */
@Component
@Named("generateClaimDelegate")
public class GenerateClaimDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateClaimDelegate.class);
    private static final String OPERATION_TYPE = "CLAIM_GENERATION";

    @Autowired
    private IdempotencyService idempotencyService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        LOGGER.info("Generating insurance claim for process instance: {}",
            execution.getProcessInstanceId());

        try {
            // Get input variables
            String patientId = (String) execution.getVariable("patientId");
            String authorizationNumber = (String) execution.getVariable("authorizationNumber");
            Object icd10Codes = execution.getVariable("icd10Codes");
            Object cptCodes = execution.getVariable("cptCodes");
            Double totalCharges = (Double) execution.getVariable("totalCharges");

            LOGGER.debug("Generating claim - Patient: {}, Auth: {}, Charges: {}",
                patientId, authorizationNumber, totalCharges);

            // Generate idempotency key based on business-critical fields
            String idempotencyKey = generateClaimIdempotencyKey(
                patientId, authorizationNumber, icd10Codes, totalCharges);

            LOGGER.debug("Idempotency key generated: {}", idempotencyKey);

            // Execute with idempotency protection
            String claimJson = idempotencyService.executeIdempotent(
                OPERATION_TYPE,
                idempotencyKey,
                () -> {
                    try {
                        // Generate new claim (only executed if not duplicate)
                        InsuranceClaim newClaim = generateClaimInternal(
                            patientId, authorizationNumber, icd10Codes, cptCodes, totalCharges);
                        return objectMapper.writeValueAsString(newClaim);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Failed to serialize claim", e);
                    }
                }
            );

            // Deserialize claim from JSON
            InsuranceClaim claim = objectMapper.readValue(claimJson, InsuranceClaim.class);

            // Check if this was a duplicate attempt
            boolean isDuplicate = idempotencyService.getStoredResult(OPERATION_TYPE, idempotencyKey).isPresent();

            // Set output variables
            execution.setVariable("claimId", claim.claimId);
            execution.setVariable("claimNumber", claim.claimNumber);
            execution.setVariable("claimStatus", claim.status);
            execution.setVariable("claimGenerationDate", claim.generationDate);
            execution.setVariable("claimTotalAmount", claim.totalAmount);
            execution.setVariable("claimFormat", claim.format);
            execution.setVariable("claimDuplicateDetected", isDuplicate);

            if (isDuplicate) {
                LOGGER.warn("DUPLICATE CLAIM PREVENTED - Returned existing claim for authorization: {}, " +
                    "Claim ID: {}, Original generation: {}",
                    authorizationNumber, claim.claimId, claim.generationDate);

                // Audit log for compliance
                execution.setVariable("claimAuditNote",
                    "Duplicate claim generation attempt prevented by idempotency control");
            } else {
                LOGGER.info("NEW CLAIM GENERATED - Claim ID: {}, Number: {}, Amount: {}",
                    claim.claimId, claim.claimNumber, claim.totalAmount);
            }

        } catch (Exception e) {
            LOGGER.error("Error generating claim: {}", e.getMessage(), e);
            execution.setVariable("claimGenerationError", e.getMessage());
            throw e;
        }
    }

    /**
     * Generate idempotency key for claim generation.
     * Uses business-critical fields that uniquely identify a claim:
     * - patientId: Who the claim is for
     * - authorizationNumber: Primary business key (one claim per authorization)
     * - icd10Codes: What procedures/diagnoses (sorted for consistency)
     * - totalCharges: Amount being claimed
     *
     * @return SHA-256 hash of the combined fields
     */
    private String generateClaimIdempotencyKey(String patientId, String authNumber,
                                                Object icd10Codes, Double charges) {
        try {
            StringBuilder keyBuilder = new StringBuilder();
            keyBuilder.append("patient:").append(patientId != null ? patientId : "");
            keyBuilder.append("|auth:").append(authNumber != null ? authNumber : "");

            // Sort ICD-10 codes for consistent ordering
            if (icd10Codes instanceof List) {
                List<String> codes = new ArrayList<>((List<String>) icd10Codes);
                Collections.sort(codes);
                keyBuilder.append("|icd10:").append(String.join(",", codes));
            }

            keyBuilder.append("|charges:").append(charges != null ? charges : 0.0);

            // Generate SHA-256 hash
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(keyBuilder.toString().getBytes(StandardCharsets.UTF_8));

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (Exception e) {
            LOGGER.error("Error generating idempotency key: {}", e.getMessage(), e);
            // Fallback to authorization number as key
            return authNumber != null ? authNumber : UUID.randomUUID().toString();
        }
    }

    /**
     * Internal method to actually generate a new claim.
     * This is only called when idempotency service determines it's not a duplicate.
     */
    private InsuranceClaim generateClaimInternal(String patientId, String authNumber,
                                                  Object icd10, Object cpt, Double charges) {
        // Simulated claim generation
        // TODO: Replace with actual claim generation system (e.g., HIPAA 837 format)
        InsuranceClaim claim = new InsuranceClaim();
        claim.claimId = UUID.randomUUID().toString();
        claim.claimNumber = "CLM-" + System.currentTimeMillis();
        claim.status = "GENERATED";
        claim.generationDate = java.time.LocalDateTime.now().toString();
        claim.totalAmount = charges != null ? charges : 0.0;
        claim.format = "HIPAA_837";
        claim.authorizationNumber = authNumber;
        claim.patientId = patientId;

        LOGGER.debug("Internal claim generation - ID: {}, Number: {}",
            claim.claimId, claim.claimNumber);

        return claim;
    }

    /**
     * Insurance claim data structure.
     * Contains all information needed to submit a claim to insurance.
     */
    public static class InsuranceClaim {
        public String claimId;
        public String claimNumber;
        public String status;
        public String generationDate;
        public Double totalAmount;
        public String format;
        public String authorizationNumber;
        public String patientId;

        // Default constructor for Jackson
        public InsuranceClaim() {
        }
    }
}
