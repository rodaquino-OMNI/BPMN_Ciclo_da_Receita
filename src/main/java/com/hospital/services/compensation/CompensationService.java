package com.hospital.services.compensation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Compensation Service Infrastructure
 *
 * Provides centralized compensation tracking, history, and idempotency management
 * for SAGA pattern implementation across all revenue cycle processes.
 */
@Service
public class CompensationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompensationService.class);

    // In-memory compensation tracking (should be replaced with database in production)
    private final Map<String, CompensationRecord> compensationHistory = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> processCompensations = new ConcurrentHashMap<>();

    /**
     * Checks if a specific compensation has already been performed
     *
     * @param processInstanceId Process instance identifier
     * @param compensationType Type of compensation (CODING, ELIGIBILITY, CLAIM, ALLOCATION)
     * @return true if compensation already performed, false otherwise
     */
    public boolean isCompensationPerformed(String processInstanceId, String compensationType) {
        String key = generateCompensationKey(processInstanceId, compensationType);
        boolean performed = compensationHistory.containsKey(key);

        if (performed) {
            LOGGER.info("IDEMPOTENCY CHECK: Compensation {} already performed for process: {}",
                compensationType, processInstanceId);
        }

        return performed;
    }

    /**
     * Records a compensation action
     *
     * @param processInstanceId Process instance identifier
     * @param compensationType Type of compensation
     * @param entityId Entity being compensated (claimId, transactionId, etc.)
     * @param reason Reason for compensation
     * @param success Whether compensation was successful
     */
    public void recordCompensation(String processInstanceId, String compensationType,
                                   String entityId, String reason, boolean success) {
        String key = generateCompensationKey(processInstanceId, compensationType);

        CompensationRecord record = new CompensationRecord(
            processInstanceId,
            compensationType,
            entityId,
            reason,
            success,
            LocalDateTime.now()
        );

        compensationHistory.put(key, record);

        // Track all compensations for this process
        processCompensations.computeIfAbsent(processInstanceId, k -> new HashSet<>())
            .add(compensationType);

        LOGGER.warn("COMPENSATION RECORDED: Type: {}, Process: {}, Entity: {}, Success: {}",
            compensationType, processInstanceId, entityId, success);
    }

    /**
     * Gets compensation history for a specific process
     *
     * @param processInstanceId Process instance identifier
     * @return List of compensation records for the process
     */
    public List<CompensationRecord> getProcessCompensationHistory(String processInstanceId) {
        List<CompensationRecord> history = new ArrayList<>();

        Set<String> compensationTypes = processCompensations.get(processInstanceId);
        if (compensationTypes != null) {
            for (String type : compensationTypes) {
                String key = generateCompensationKey(processInstanceId, type);
                CompensationRecord record = compensationHistory.get(key);
                if (record != null) {
                    history.add(record);
                }
            }
        }

        return history;
    }

    /**
     * Gets all compensations of a specific type
     *
     * @param compensationType Type of compensation
     * @return List of compensation records of that type
     */
    public List<CompensationRecord> getCompensationsByType(String compensationType) {
        List<CompensationRecord> records = new ArrayList<>();

        for (CompensationRecord record : compensationHistory.values()) {
            if (record.getCompensationType().equals(compensationType)) {
                records.add(record);
            }
        }

        return records;
    }

    /**
     * Clears compensation history for a process (use with caution)
     *
     * @param processInstanceId Process instance identifier
     */
    public void clearProcessHistory(String processInstanceId) {
        Set<String> types = processCompensations.remove(processInstanceId);

        if (types != null) {
            for (String type : types) {
                String key = generateCompensationKey(processInstanceId, type);
                compensationHistory.remove(key);
            }
        }

        LOGGER.info("Compensation history cleared for process: {}", processInstanceId);
    }

    /**
     * Gets compensation statistics
     *
     * @return Map of compensation statistics
     */
    public Map<String, Object> getCompensationStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalCompensations", compensationHistory.size());
        stats.put("totalProcesses", processCompensations.size());

        // Count by type
        Map<String, Long> typeCount = new HashMap<>();
        for (CompensationRecord record : compensationHistory.values()) {
            typeCount.merge(record.getCompensationType(), 1L, Long::sum);
        }
        stats.put("compensationsByType", typeCount);

        // Count successful vs failed
        long successful = compensationHistory.values().stream()
            .filter(CompensationRecord::isSuccess)
            .count();
        stats.put("successfulCompensations", successful);
        stats.put("failedCompensations", compensationHistory.size() - successful);

        return stats;
    }

    /**
     * Generates unique key for compensation tracking
     */
    private String generateCompensationKey(String processInstanceId, String compensationType) {
        return processInstanceId + "_" + compensationType;
    }

    /**
     * Compensation Record Data Class
     */
    public static class CompensationRecord {
        private final String processInstanceId;
        private final String compensationType;
        private final String entityId;
        private final String reason;
        private final boolean success;
        private final LocalDateTime timestamp;

        public CompensationRecord(String processInstanceId, String compensationType,
                                 String entityId, String reason, boolean success,
                                 LocalDateTime timestamp) {
            this.processInstanceId = processInstanceId;
            this.compensationType = compensationType;
            this.entityId = entityId;
            this.reason = reason;
            this.success = success;
            this.timestamp = timestamp;
        }

        public String getProcessInstanceId() { return processInstanceId; }
        public String getCompensationType() { return compensationType; }
        public String getEntityId() { return entityId; }
        public String getReason() { return reason; }
        public boolean isSuccess() { return success; }
        public LocalDateTime getTimestamp() { return timestamp; }

        @Override
        public String toString() {
            return String.format("CompensationRecord[process=%s, type=%s, entity=%s, success=%s, timestamp=%s]",
                processInstanceId, compensationType, entityId, success, timestamp);
        }
    }
}
