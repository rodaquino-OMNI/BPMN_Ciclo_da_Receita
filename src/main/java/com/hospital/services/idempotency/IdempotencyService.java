package com.hospital.services.idempotency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Service for managing idempotent operations in the Hospital Revenue Cycle system.
 *
 * <p>This service ensures that operations are executed exactly once, even if the same
 * request is made multiple times. It uses a database-backed idempotency key store
 * with optimistic locking to handle concurrent requests safely.</p>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>
 * String patientId = idempotencyService.executeIdempotent(
 *     "REGISTER_PATIENT",
 *     idempotencyKeyGenerator.generate(variables),
 *     () -&gt; patientService.registerPatient(patientData)
 * );
 * </pre>
 *
 * @author Hospital Revenue Cycle System
 * @version 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyKeyRepository repository;
    private final ObjectMapper objectMapper;

    /**
     * Maximum number of retry attempts for handling optimistic locking failures.
     */
    private static final int MAX_RETRY_ATTEMPTS = 3;

    /**
     * Default expiration time for idempotency keys (24 hours).
     */
    private static final int DEFAULT_EXPIRATION_HOURS = 24;

    /**
     * Executes an operation idempotently with automatic retry on conflicts.
     *
     * <p>If the operation has already been executed, returns the stored result.
     * Otherwise, executes the operation and stores the result for future requests.</p>
     *
     * @param <T> the return type of the operation
     * @param operationType the type of operation (e.g., "REGISTER_PATIENT")
     * @param operationKey the unique key for this operation instance
     * @param operation the operation to execute
     * @return the result of the operation (either newly computed or previously stored)
     * @throws IdempotencyException if the operation cannot be completed
     */
    public <T> T executeIdempotent(String operationType, String operationKey, Supplier<T> operation) {
        return executeIdempotent(operationType, operationKey, operation, null, null);
    }

    /**
     * Executes an operation idempotently with process context.
     *
     * @param <T> the return type of the operation
     * @param operationType the type of operation
     * @param operationKey the unique key for this operation instance
     * @param operation the operation to execute
     * @param processInstanceId the Camunda process instance ID (optional)
     * @param executionId the Camunda execution ID (optional)
     * @return the result of the operation
     * @throws IdempotencyException if the operation cannot be completed
     */
    public <T> T executeIdempotent(
            String operationType,
            String operationKey,
            Supplier<T> operation,
            String processInstanceId,
            String executionId) {

        log.debug("Executing idempotent operation: type={}, key={}, processInstance={}",
                  operationType, operationKey, processInstanceId);

        int attempt = 0;
        while (attempt < MAX_RETRY_ATTEMPTS) {
            try {
                return executeIdempotentInternal(operationType, operationKey, operation,
                                                 processInstanceId, executionId);
            } catch (ObjectOptimisticLockingFailureException | DataIntegrityViolationException e) {
                attempt++;
                log.warn("Concurrent modification detected for operation {}:{}, attempt {}/{}",
                        operationType, operationKey, attempt, MAX_RETRY_ATTEMPTS);

                if (attempt >= MAX_RETRY_ATTEMPTS) {
                    log.error("Max retry attempts exceeded for operation {}:{}",
                             operationType, operationKey);
                    throw new IdempotencyException(
                        "Failed to execute idempotent operation after " + MAX_RETRY_ATTEMPTS + " attempts",
                        operationType,
                        operationKey,
                        null
                    );
                }

                // Brief pause before retry
                try {
                    Thread.sleep(50 * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IdempotencyException(
                        "Interrupted during retry",
                        operationType,
                        operationKey,
                        null
                    );
                }
            }
        }

        throw new IdempotencyException(
            "Unexpected error in idempotent execution",
            operationType,
            operationKey,
            null
        );
    }

    /**
     * Internal method that performs the actual idempotent execution with transaction control.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected <T> T executeIdempotentInternal(
            String operationType,
            String operationKey,
            Supplier<T> operation,
            String processInstanceId,
            String executionId) {

        // Check if operation already executed
        Optional<IdempotencyKey> existingKey = repository.findByOperationTypeAndOperationKey(
            operationType, operationKey);

        if (existingKey.isPresent()) {
            IdempotencyKey key = existingKey.get();

            if (key.isCompleted()) {
                log.info("Operation already completed: {}:{}, returning stored result",
                        operationType, operationKey);
                return deserializeResult(key.getResult());
            }

            if (key.getStatus() == IdempotencyKey.IdempotencyStatus.PROCESSING) {
                log.warn("Operation already in progress: {}:{}, waiting for completion",
                        operationType, operationKey);
                // In a real system, you might want to poll or use a lock here
                throw new IdempotencyException(
                    "Operation is already being processed",
                    operationType,
                    operationKey,
                    null
                );
            }
        }

        // Create new idempotency key
        IdempotencyKey key = IdempotencyKey.builder()
            .operationType(operationType)
            .operationKey(operationKey)
            .processInstanceId(processInstanceId)
            .executionId(executionId)
            .status(IdempotencyKey.IdempotencyStatus.PROCESSING)
            .createdAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusHours(DEFAULT_EXPIRATION_HOURS))
            .build();

        repository.save(key);
        repository.flush(); // Force immediate persistence to detect conflicts

        try {
            // Execute the operation
            T result = operation.get();

            // Store the result
            String serializedResult = serializeResult(result);
            key.markCompleted(serializedResult);
            repository.save(key);

            log.info("Operation completed successfully: {}:{}", operationType, operationKey);
            return result;

        } catch (Exception e) {
            log.error("Operation failed: {}:{}", operationType, operationKey, e);
            key.markFailed();
            repository.save(key);
            throw new IdempotencyException(
                "Operation execution failed: " + e.getMessage(),
                operationType,
                operationKey,
                null,
                e
            );
        }
    }

    /**
     * Retrieves a previously stored result for an operation.
     *
     * @param operationType the type of operation
     * @param operationKey the unique key for this operation instance
     * @return an Optional containing the stored result if available
     */
    public Optional<String> getStoredResult(String operationType, String operationKey) {
        log.debug("Retrieving stored result for operation: {}:{}", operationType, operationKey);

        return repository.findByOperationTypeAndOperationKey(operationType, operationKey)
            .filter(IdempotencyKey::isCompleted)
            .map(IdempotencyKey::getResult);
    }

    /**
     * Manually stores a result for an operation.
     *
     * <p>This is useful for operations that are executed outside the normal
     * idempotency flow but still need to be tracked.</p>
     *
     * @param operationType the type of operation
     * @param operationKey the unique key for this operation instance
     * @param result the result to store
     */
    @Transactional
    public void storeResult(String operationType, String operationKey, String result) {
        log.debug("Manually storing result for operation: {}:{}", operationType, operationKey);

        Optional<IdempotencyKey> existingKey = repository.findByOperationTypeAndOperationKey(
            operationType, operationKey);

        if (existingKey.isPresent()) {
            IdempotencyKey key = existingKey.get();
            key.markCompleted(result);
            repository.save(key);
        } else {
            IdempotencyKey key = IdempotencyKey.builder()
                .operationType(operationType)
                .operationKey(operationKey)
                .status(IdempotencyKey.IdempotencyStatus.COMPLETED)
                .result(result)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(DEFAULT_EXPIRATION_HOURS))
                .build();
            repository.save(key);
        }
    }

    /**
     * Cleans up expired idempotency keys.
     *
     * <p>This method should be called periodically (e.g., via a scheduled task)
     * to prevent unbounded database growth.</p>
     *
     * @return the number of deleted keys
     */
    @Transactional
    public int cleanupExpiredKeys() {
        LocalDateTime cutoff = LocalDateTime.now();
        int deleted = repository.deleteByExpiresAtBefore(cutoff);
        log.info("Cleaned up {} expired idempotency keys", deleted);
        return deleted;
    }

    /**
     * Finds and cleans up idempotency keys that are stuck in PROCESSING status.
     *
     * @param timeoutMinutes the timeout in minutes for PROCESSING status
     * @return the number of cleaned up keys
     */
    @Transactional
    public int cleanupStuckKeys(int timeoutMinutes) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(timeoutMinutes);
        var stuckKeys = repository.findStuckProcessingKeys(cutoff);

        stuckKeys.forEach(key -> {
            log.warn("Found stuck idempotency key: {}:{}, marking as failed",
                    key.getOperationType(), key.getOperationKey());
            key.markFailed();
            repository.save(key);
        });

        return stuckKeys.size();
    }

    /**
     * Serializes a result object to JSON string.
     */
    private String serializeResult(Object result) {
        if (result == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize result", e);
            throw new IdempotencyException(
                "Failed to serialize operation result",
                null,
                null,
                null,
                e
            );
        }
    }

    /**
     * Deserializes a result from JSON string.
     */
    @SuppressWarnings("unchecked")
    private <T> T deserializeResult(String serializedResult) {
        if (serializedResult == null) {
            return null;
        }

        try {
            return (T) objectMapper.readValue(serializedResult, Object.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize result", e);
            throw new IdempotencyException(
                "Failed to deserialize stored result",
                null,
                null,
                serializedResult,
                e
            );
        }
    }
}
