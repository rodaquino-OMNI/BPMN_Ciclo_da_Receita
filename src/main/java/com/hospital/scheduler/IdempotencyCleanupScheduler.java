package com.hospital.scheduler;

import com.hospital.services.idempotency.IdempotencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Scheduled task to clean up expired idempotency records.
 *
 * <p>Uses ShedLock to prevent concurrent execution across multiple instances
 * and ensure only one cleanup runs at a time, even with multiple schedules.</p>
 *
 * <p><strong>Scheduling Strategy:</strong></p>
 * <ul>
 *   <li>Primary: Cron-based daily cleanup at 2:00 AM</li>
 *   <li>Backup: Fixed-rate cleanup every 6 hours for high-volume systems</li>
 *   <li>Both use the SAME lock name to prevent concurrent execution</li>
 * </ul>
 *
 * @author Hospital Revenue Cycle System
 * @version 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotencyCleanupScheduler {

    private final IdempotencyService idempotencyService;

    /**
     * Clean up expired idempotency records - Daily schedule.
     *
     * <p>Runs daily at 2:00 AM with distributed locking to prevent concurrent execution.</p>
     *
     * <p><strong>Lock Configuration:</strong></p>
     * <ul>
     *   <li>Lock name: "idempotencyCleanup" (shared with frequent cleanup)</li>
     *   <li>Max duration: 4 hours (prevents stuck locks)</li>
     *   <li>Min duration: 1 minute (prevents too-frequent executions)</li>
     * </ul>
     */
    @Transactional(timeout = 14400) // 4 hour transaction timeout
    @Scheduled(cron = "${idempotency.cleanup.cron:0 0 2 * * ?}")
    @SchedulerLock(
        name = "idempotencyCleanup",
        lockAtMostFor = "4h",
        lockAtLeastFor = "1m"
    )
    public void cleanupExpiredRecordsDaily() {
        log.debug("Starting daily cleanup of expired idempotency records");
        long startTime = System.currentTimeMillis();

        try {
            int deletedCount = idempotencyService.cleanupExpiredKeys();
            long durationMs = System.currentTimeMillis() - startTime;

            if (deletedCount > 0) {
                log.info("Daily cleanup completed - Deleted {} expired records in {}ms",
                         deletedCount, durationMs);
            } else {
                log.debug("Daily cleanup completed - No expired records found");
            }

        } catch (DataAccessException e) {
            log.error("Database error during daily cleanup: {}", e.getMessage(), e);
            throw e; // Re-throw to trigger transaction rollback

        } catch (Exception e) {
            log.error("Unexpected error during daily cleanup: {}", e.getMessage(), e);
            throw e; // Re-throw to trigger transaction rollback
        }
    }

    /**
     * Clean up expired records every 6 hours (backup/high-volume schedule).
     *
     * <p>Provides more frequent cleanup for high-volume systems. Uses the SAME lock
     * as the daily cleanup to prevent concurrent execution.</p>
     *
     * <p><strong>Key Feature:</strong> If this schedule runs at the same time as the
     * daily schedule (e.g., both at 2:00 AM), only ONE will acquire the lock and execute.
     * The other will skip execution gracefully.</p>
     *
     * <p><strong>Configuration:</strong></p>
     * <ul>
     *   <li>Frequency: Every 6 hours (configurable via properties)</li>
     *   <li>Initial delay: 1 hour after startup (configurable)</li>
     *   <li>Lock: Shared with daily cleanup (prevents collision)</li>
     * </ul>
     */
    @Transactional(timeout = 14400) // 4 hour transaction timeout
    @Scheduled(
        fixedRateString = "${idempotency.cleanup.fixed-rate:21600000}",
        initialDelayString = "${idempotency.cleanup.initial-delay:3600000}"
    )
    @SchedulerLock(
        name = "idempotencyCleanup", // SAME lock name as daily cleanup
        lockAtMostFor = "4h",
        lockAtLeastFor = "1m"
    )
    public void cleanupExpiredRecordsFrequent() {
        log.debug("Starting frequent cleanup of expired idempotency records");
        long startTime = System.currentTimeMillis();

        try {
            int deletedCount = idempotencyService.cleanupExpiredKeys();
            long durationMs = System.currentTimeMillis() - startTime;

            if (deletedCount > 100) {
                log.info("Frequent cleanup completed - Deleted {} expired records in {}ms",
                         deletedCount, durationMs);
            } else if (deletedCount > 0) {
                log.debug("Frequent cleanup completed - Deleted {} expired records", deletedCount);
            }

        } catch (DataAccessException e) {
            log.error("Database error during frequent cleanup: {}", e.getMessage(), e);
            throw e; // Re-throw to trigger transaction rollback

        } catch (Exception e) {
            log.error("Unexpected error during frequent cleanup: {}", e.getMessage(), e);
            throw e; // Re-throw to trigger transaction rollback
        }
    }
}
