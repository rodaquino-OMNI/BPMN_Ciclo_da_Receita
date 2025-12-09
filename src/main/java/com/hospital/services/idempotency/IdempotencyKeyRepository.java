package com.hospital.services.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Spring Data JPA Repository for {@link IdempotencyKey} entities.
 *
 * <p>Provides CRUD operations and custom queries for managing idempotency keys
 * in the database. Includes cleanup methods for expired keys.</p>
 *
 * @author Hospital Revenue Cycle System
 * @version 1.0
 */
@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {

    /**
     * Finds an idempotency key by operation type and operation key.
     *
     * <p>This method is used to check if an operation has already been executed
     * and to retrieve the stored result.</p>
     *
     * @param operationType the type of operation
     * @param operationKey the unique key for this operation instance
     * @return an Optional containing the IdempotencyKey if found
     */
    @Query("SELECT ik FROM IdempotencyKey ik WHERE ik.operationType = :operationType AND ik.operationKey = :operationKey")
    Optional<IdempotencyKey> findByOperationTypeAndOperationKey(
        @Param("operationType") String operationType,
        @Param("operationKey") String operationKey
    );

    /**
     * Deletes all idempotency keys that have expired before the given timestamp.
     *
     * <p>This method should be called periodically to clean up old idempotency keys
     * and prevent unbounded database growth.</p>
     *
     * @param timestamp the cutoff timestamp for deletion
     * @return the number of deleted records
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM IdempotencyKey ik WHERE ik.expiresAt < :timestamp")
    int deleteByExpiresAtBefore(@Param("timestamp") LocalDateTime timestamp);

    /**
     * Finds all idempotency keys for a specific process instance.
     *
     * <p>Useful for debugging and auditing process execution.</p>
     *
     * @param processInstanceId the Camunda process instance ID
     * @return a list of IdempotencyKey entities
     */
    @Query("SELECT ik FROM IdempotencyKey ik WHERE ik.processInstanceId = :processInstanceId ORDER BY ik.createdAt DESC")
    java.util.List<IdempotencyKey> findByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

    /**
     * Counts idempotency keys by status.
     *
     * <p>Useful for monitoring and metrics collection.</p>
     *
     * @param status the status to count
     * @return the count of keys with the given status
     */
    @Query("SELECT COUNT(ik) FROM IdempotencyKey ik WHERE ik.status = :status")
    long countByStatus(@Param("status") IdempotencyKey.IdempotencyStatus status);

    /**
     * Finds idempotency keys that are stuck in PROCESSING status for too long.
     *
     * <p>This can indicate failed operations that didn't properly update their status.
     * Useful for cleanup and alerting.</p>
     *
     * @param cutoffTime the time before which PROCESSING keys are considered stuck
     * @return a list of potentially stuck IdempotencyKey entities
     */
    @Query("SELECT ik FROM IdempotencyKey ik WHERE ik.status = 'PROCESSING' AND ik.createdAt < :cutoffTime")
    java.util.List<IdempotencyKey> findStuckProcessingKeys(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Deletes idempotency keys for a specific process instance.
     *
     * <p>Used for cleanup when a process instance is deleted.</p>
     *
     * @param processInstanceId the Camunda process instance ID
     * @return the number of deleted records
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM IdempotencyKey ik WHERE ik.processInstanceId = :processInstanceId")
    int deleteByProcessInstanceId(@Param("processInstanceId") String processInstanceId);
}
