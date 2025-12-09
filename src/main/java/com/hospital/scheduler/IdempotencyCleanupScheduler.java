package com.hospital.scheduler;

import com.hospital.services.idempotency.IdempotencyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task to clean up expired idempotency records
 * Runs daily to prevent unbounded growth of idempotency_records table
 */
@Component
public class IdempotencyCleanupScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdempotencyCleanupScheduler.class);

    @Autowired
    private IdempotencyService idempotencyService;

    /**
     * Clean up expired idempotency records
     * Runs daily at 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredRecords() {
        LOGGER.info("Starting scheduled cleanup of expired idempotency records");

        try {
            int deletedCount = idempotencyService.cleanupExpiredKeys();
            LOGGER.info("Scheduled cleanup completed - Deleted {} expired records", deletedCount);
        } catch (Exception e) {
            LOGGER.error("Error during scheduled idempotency cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * Clean up expired records every 6 hours (backup schedule)
     * Provides more frequent cleanup for high-volume systems
     */
    @Scheduled(fixedRate = 21600000, initialDelay = 3600000) // 6 hours in milliseconds
    public void cleanupExpiredRecordsFrequent() {
        LOGGER.debug("Starting frequent cleanup of expired idempotency records");

        try {
            int deletedCount = idempotencyService.cleanupExpiredKeys();
            if (deletedCount > 0) {
                LOGGER.info("Frequent cleanup completed - Deleted {} expired records", deletedCount);
            }
        } catch (Exception e) {
            LOGGER.error("Error during frequent idempotency cleanup: {}", e.getMessage(), e);
        }
    }
}
