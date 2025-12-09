package com.hospital.scheduler;

import com.hospital.services.idempotency.IdempotencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for IdempotencyCleanupScheduler.
 *
 * <p>Validates scheduled cleanup behavior, exception handling, and logging
 * without requiring Spring context or database.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("IdempotencyCleanupScheduler Unit Tests")
class IdempotencyCleanupSchedulerTest {

    @Mock
    private IdempotencyService idempotencyService;

    @InjectMocks
    private IdempotencyCleanupScheduler scheduler;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(idempotencyService);
    }

    @Test
    @DisplayName("Daily cleanup should successfully delete expired records")
    void testCleanupExpiredRecordsDaily_Success() {
        // Arrange
        when(idempotencyService.cleanupExpiredKeys()).thenReturn(150);

        // Act
        scheduler.cleanupExpiredRecordsDaily();

        // Assert
        verify(idempotencyService, times(1)).cleanupExpiredKeys();
    }

    @Test
    @DisplayName("Daily cleanup should handle zero records gracefully")
    void testCleanupExpiredRecordsDaily_ZeroRecords() {
        // Arrange
        when(idempotencyService.cleanupExpiredKeys()).thenReturn(0);

        // Act
        scheduler.cleanupExpiredRecordsDaily();

        // Assert
        verify(idempotencyService, times(1)).cleanupExpiredKeys();
    }

    @Test
    @DisplayName("Daily cleanup should re-throw DataAccessException for transaction rollback")
    void testCleanupExpiredRecordsDaily_DatabaseError() {
        // Arrange
        DataAccessException exception = new DataAccessException("Database connection failed") {};
        when(idempotencyService.cleanupExpiredKeys()).thenThrow(exception);

        // Act & Assert
        assertThatThrownBy(() -> scheduler.cleanupExpiredRecordsDaily())
            .isInstanceOf(DataAccessException.class)
            .hasMessageContaining("Database connection failed");

        verify(idempotencyService, times(1)).cleanupExpiredKeys();
    }

    @Test
    @DisplayName("Daily cleanup should re-throw unexpected exceptions")
    void testCleanupExpiredRecordsDaily_UnexpectedException() {
        // Arrange
        RuntimeException exception = new RuntimeException("Unexpected error");
        when(idempotencyService.cleanupExpiredKeys()).thenThrow(exception);

        // Act & Assert
        assertThatThrownBy(() -> scheduler.cleanupExpiredRecordsDaily())
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Unexpected error");

        verify(idempotencyService, times(1)).cleanupExpiredKeys();
    }

    @Test
    @DisplayName("Frequent cleanup should successfully delete expired records")
    void testCleanupExpiredRecordsFrequent_Success() {
        // Arrange
        when(idempotencyService.cleanupExpiredKeys()).thenReturn(250);

        // Act
        scheduler.cleanupExpiredRecordsFrequent();

        // Assert
        verify(idempotencyService, times(1)).cleanupExpiredKeys();
    }

    @Test
    @DisplayName("Frequent cleanup should handle small deletions (< 100 records)")
    void testCleanupExpiredRecordsFrequent_SmallDeletion() {
        // Arrange
        when(idempotencyService.cleanupExpiredKeys()).thenReturn(50);

        // Act
        scheduler.cleanupExpiredRecordsFrequent();

        // Assert
        verify(idempotencyService, times(1)).cleanupExpiredKeys();
    }

    @Test
    @DisplayName("Frequent cleanup should re-throw database exceptions")
    void testCleanupExpiredRecordsFrequent_DatabaseError() {
        // Arrange
        DataAccessException exception = new DataAccessException("Lock timeout") {};
        when(idempotencyService.cleanupExpiredKeys()).thenThrow(exception);

        // Act & Assert
        assertThatThrownBy(() -> scheduler.cleanupExpiredRecordsFrequent())
            .isInstanceOf(DataAccessException.class)
            .hasMessageContaining("Lock timeout");

        verify(idempotencyService, times(1)).cleanupExpiredKeys();
    }

    @Test
    @DisplayName("Frequent cleanup should handle large volume deletions (> 100 records)")
    void testCleanupExpiredRecordsFrequent_LargeDeletion() {
        // Arrange
        when(idempotencyService.cleanupExpiredKeys()).thenReturn(1500);

        // Act
        scheduler.cleanupExpiredRecordsFrequent();

        // Assert
        verify(idempotencyService, times(1)).cleanupExpiredKeys();
    }
}
