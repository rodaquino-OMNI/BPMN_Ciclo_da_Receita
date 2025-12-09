package com.hospital.scheduler;

import com.hospital.RevenueCycleApplication;
import com.hospital.services.idempotency.IdempotencyKey;
import com.hospital.services.idempotency.IdempotencyKeyRepository;
import net.javacrumbs.shedlock.core.LockProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for IdempotencyCleanupScheduler.
 *
 * <p>Tests real Spring context initialization, bean wiring, scheduling configuration,
 * and distributed locking behavior with actual database.</p>
 */
@SpringBootTest(classes = RevenueCycleApplication.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("IdempotencyCleanupScheduler Integration Tests")
class IdempotencyCleanupSchedulerIntegrationTest {

    @Autowired
    private IdempotencyCleanupScheduler scheduler;

    @Autowired
    private IdempotencyKeyRepository repository;

    @Autowired(required = false)
    private LockProvider lockProvider;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean database before each test
        repository.deleteAll();
    }

    @Test
    @DisplayName("Scheduler bean should be properly initialized and autowired")
    void testSchedulerBeanInitialization() {
        assertThat(scheduler).isNotNull();
        assertThat(lockProvider).isNotNull()
            .as("ShedLock LockProvider should be configured");
    }

    @Test
    @DisplayName("Daily cleanup should delete expired records from database")
    @Transactional
    void testCleanupExpiredRecordsDaily_DeletesExpiredRecords() {
        // Arrange: Create expired idempotency keys
        createExpiredIdempotencyKey("OPERATION_1", "key1", LocalDateTime.now().minusDays(2));
        createExpiredIdempotencyKey("OPERATION_2", "key2", LocalDateTime.now().minusHours(25));
        createExpiredIdempotencyKey("OPERATION_3", "key3", LocalDateTime.now().minusMinutes(1));

        // Create non-expired keys
        createIdempotencyKey("OPERATION_4", "key4", LocalDateTime.now().plusHours(1));
        createIdempotencyKey("OPERATION_5", "key5", LocalDateTime.now().plusDays(1));

        assertThat(repository.count()).isEqualTo(5);

        // Act
        scheduler.cleanupExpiredRecordsDaily();

        // Assert
        assertThat(repository.count()).isEqualTo(2)
            .as("Only non-expired keys should remain");

        var remainingKeys = repository.findAll();
        assertThat(remainingKeys)
            .extracting(IdempotencyKey::getOperationKey)
            .containsExactlyInAnyOrder("key4", "key5");
    }

    @Test
    @DisplayName("Frequent cleanup should delete same expired records")
    @Transactional
    void testCleanupExpiredRecordsFrequent_DeletesExpiredRecords() {
        // Arrange
        createExpiredIdempotencyKey("OPERATION_1", "key1", LocalDateTime.now().minusDays(3));
        createExpiredIdempotencyKey("OPERATION_2", "key2", LocalDateTime.now().minusHours(48));

        createIdempotencyKey("OPERATION_3", "key3", LocalDateTime.now().plusHours(2));

        assertThat(repository.count()).isEqualTo(3);

        // Act
        scheduler.cleanupExpiredRecordsFrequent();

        // Assert
        assertThat(repository.count()).isEqualTo(1)
            .as("Only non-expired key should remain");

        var remainingKeys = repository.findAll();
        assertThat(remainingKeys)
            .extracting(IdempotencyKey::getOperationKey)
            .containsExactly("key3");
    }

    @Test
    @DisplayName("Cleanup should handle empty database gracefully")
    void testCleanup_EmptyDatabase() {
        // Arrange
        assertThat(repository.count()).isZero();

        // Act
        scheduler.cleanupExpiredRecordsDaily();

        // Assert
        assertThat(repository.count()).isZero();
    }

    @Test
    @DisplayName("Cleanup should handle database with no expired records")
    @Transactional
    void testCleanup_NoExpiredRecords() {
        // Arrange: Create only non-expired keys
        createIdempotencyKey("OPERATION_1", "key1", LocalDateTime.now().plusHours(5));
        createIdempotencyKey("OPERATION_2", "key2", LocalDateTime.now().plusDays(1));

        assertThat(repository.count()).isEqualTo(2);

        // Act
        scheduler.cleanupExpiredRecordsDaily();

        // Assert
        assertThat(repository.count()).isEqualTo(2)
            .as("No records should be deleted");
    }

    @Test
    @DisplayName("Cleanup should handle large volume of expired records")
    @Transactional
    void testCleanup_LargeVolume() {
        // Arrange: Create 100 expired keys
        for (int i = 0; i < 100; i++) {
            createExpiredIdempotencyKey("OPERATION_" + i, "key" + i,
                LocalDateTime.now().minusDays(i + 1));
        }

        // Create 10 non-expired keys
        for (int i = 100; i < 110; i++) {
            createIdempotencyKey("OPERATION_" + i, "key" + i,
                LocalDateTime.now().plusHours(i - 99));
        }

        assertThat(repository.count()).isEqualTo(110);

        // Act
        scheduler.cleanupExpiredRecordsFrequent();

        // Assert
        assertThat(repository.count()).isEqualTo(10)
            .as("Only non-expired keys should remain");
    }

    // Helper methods

    private void createIdempotencyKey(String operationType, String operationKey, LocalDateTime expiresAt) {
        IdempotencyKey key = IdempotencyKey.builder()
            .operationType(operationType)
            .operationKey(operationKey)
            .status(IdempotencyKey.IdempotencyStatus.COMPLETED)
            .result("test-result")
            .createdAt(LocalDateTime.now())
            .expiresAt(expiresAt)
            .build();
        repository.save(key);
    }

    private void createExpiredIdempotencyKey(String operationType, String operationKey, LocalDateTime expiresAt) {
        createIdempotencyKey(operationType, operationKey, expiresAt);
    }
}
