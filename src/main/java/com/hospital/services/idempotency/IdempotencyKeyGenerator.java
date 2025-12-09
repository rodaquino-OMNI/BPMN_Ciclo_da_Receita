package com.hospital.services.idempotency;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for generating deterministic idempotency keys.
 *
 * <p>This class generates consistent keys from process variables and business identifiers
 * using SHA-256 hashing. Keys are designed to be deterministic so that the same input
 * always produces the same key, enabling proper idempotency.</p>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>
 * String key = IdempotencyKeyGenerator.generateFromVariables(
 *     Map.of(
 *         "patientCPF", "12345678900",
 *         "appointmentDate", "2025-01-15",
 *         "doctorId", "DOC123"
 *     )
 * );
 * </pre>
 *
 * @author Hospital Revenue Cycle System
 * @version 1.0
 */
@Slf4j
@Component
public class IdempotencyKeyGenerator {

    private static final String HASH_ALGORITHM = "SHA-256";
    private static final String SEPARATOR = "|";

    /**
     * Generates an idempotency key from a map of variables.
     *
     * <p>The variables are sorted by key to ensure deterministic ordering,
     * then concatenated and hashed using SHA-256.</p>
     *
     * @param variables the variables to include in the key
     * @return a deterministic hash-based idempotency key
     */
    public static String generateFromVariables(Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) {
            throw new IllegalArgumentException("Variables cannot be null or empty");
        }

        // Sort keys and build deterministic string representation
        String concatenated = variables.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> entry.getKey() + "=" + String.valueOf(entry.getValue()))
            .collect(Collectors.joining(SEPARATOR));

        return hashString(concatenated);
    }

    /**
     * Generates an idempotency key from individual components.
     *
     * <p>This is useful when you have a small number of known identifiers.</p>
     *
     * @param components the components to include in the key
     * @return a deterministic hash-based idempotency key
     */
    public static String generateFromComponents(String... components) {
        if (components == null || components.length == 0) {
            throw new IllegalArgumentException("Components cannot be null or empty");
        }

        String concatenated = String.join(SEPARATOR, components);
        return hashString(concatenated);
    }

    /**
     * Generates an idempotency key for a patient registration operation.
     *
     * <p>Uses patient CPF and timestamp to ensure uniqueness while allowing
     * retry of the same registration attempt.</p>
     *
     * @param cpf the patient's CPF
     * @param timestamp the registration timestamp (ISO format)
     * @return a deterministic idempotency key
     */
    public static String generatePatientRegistrationKey(String cpf, String timestamp) {
        validateNotEmpty("cpf", cpf);
        validateNotEmpty("timestamp", timestamp);

        return generateFromComponents("PATIENT_REGISTRATION", cpf, timestamp);
    }

    /**
     * Generates an idempotency key for an invoice creation operation.
     *
     * <p>Uses patient ID, appointment ID, and date to ensure uniqueness.</p>
     *
     * @param patientId the patient identifier
     * @param appointmentId the appointment identifier
     * @param invoiceDate the invoice date (ISO format)
     * @return a deterministic idempotency key
     */
    public static String generateInvoiceCreationKey(String patientId, String appointmentId, String invoiceDate) {
        validateNotEmpty("patientId", patientId);
        validateNotEmpty("appointmentId", appointmentId);
        validateNotEmpty("invoiceDate", invoiceDate);

        return generateFromComponents("INVOICE_CREATION", patientId, appointmentId, invoiceDate);
    }

    /**
     * Generates an idempotency key for a payment processing operation.
     *
     * <p>Uses invoice ID and payment transaction reference to ensure uniqueness.</p>
     *
     * @param invoiceId the invoice identifier
     * @param transactionReference the payment transaction reference
     * @return a deterministic idempotency key
     */
    public static String generatePaymentProcessingKey(String invoiceId, String transactionReference) {
        validateNotEmpty("invoiceId", invoiceId);
        validateNotEmpty("transactionReference", transactionReference);

        return generateFromComponents("PAYMENT_PROCESSING", invoiceId, transactionReference);
    }

    /**
     * Generates an idempotency key for a notification sending operation.
     *
     * <p>Uses recipient ID, notification type, and correlation ID to ensure uniqueness.</p>
     *
     * @param recipientId the recipient identifier
     * @param notificationType the type of notification
     * @param correlationId the correlation identifier
     * @return a deterministic idempotency key
     */
    public static String generateNotificationKey(String recipientId, String notificationType, String correlationId) {
        validateNotEmpty("recipientId", recipientId);
        validateNotEmpty("notificationType", notificationType);
        validateNotEmpty("correlationId", correlationId);

        return generateFromComponents("NOTIFICATION", recipientId, notificationType, correlationId);
    }

    /**
     * Generates an idempotency key for a process instance operation.
     *
     * <p>This is useful for operations that should be idempotent within a specific
     * process instance but may be repeated across different instances.</p>
     *
     * @param processInstanceId the Camunda process instance ID
     * @param activityId the activity/task ID within the process
     * @param businessKey additional business key (optional)
     * @return a deterministic idempotency key
     */
    public static String generateProcessOperationKey(String processInstanceId, String activityId, String businessKey) {
        validateNotEmpty("processInstanceId", processInstanceId);
        validateNotEmpty("activityId", activityId);

        if (businessKey != null && !businessKey.isEmpty()) {
            return generateFromComponents("PROCESS_OPERATION", processInstanceId, activityId, businessKey);
        } else {
            return generateFromComponents("PROCESS_OPERATION", processInstanceId, activityId);
        }
    }

    /**
     * Generates an idempotency key with a custom prefix.
     *
     * <p>This allows for creating namespaced keys for custom operations.</p>
     *
     * @param prefix the prefix to use for namespacing
     * @param components the components to include in the key
     * @return a deterministic idempotency key
     */
    public static String generateWithPrefix(String prefix, String... components) {
        validateNotEmpty("prefix", prefix);

        if (components == null || components.length == 0) {
            throw new IllegalArgumentException("Components cannot be null or empty");
        }

        String[] prefixedComponents = new String[components.length + 1];
        prefixedComponents[0] = prefix;
        System.arraycopy(components, 0, prefixedComponents, 1, components.length);

        return generateFromComponents(prefixedComponents);
    }

    /**
     * Hashes a string using SHA-256 and returns the hex representation.
     *
     * @param input the input string to hash
     * @return the hexadecimal representation of the hash
     */
    private static String hashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            throw new RuntimeException("Failed to generate idempotency key", e);
        }
    }

    /**
     * Validates that a parameter is not null or empty.
     *
     * @param paramName the parameter name for error messages
     * @param value the value to validate
     * @throws IllegalArgumentException if the value is null or empty
     */
    private static void validateNotEmpty(String paramName, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(paramName + " cannot be null or empty");
        }
    }

    /**
     * Validates a generated key format.
     *
     * <p>Ensures the key is a valid hexadecimal string of expected length (64 chars for SHA-256).</p>
     *
     * @param key the key to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidKey(String key) {
        if (key == null || key.length() != 64) {
            return false;
        }

        return key.matches("^[a-f0-9]{64}$");
    }
}
