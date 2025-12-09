package com.hospital.repository;

import com.hospital.model.IdempotencyRecord;
import com.hospital.model.IdempotencyRecord.IdempotencyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing idempotency records
 */
@Repository
public interface IdempotencyRepository extends JpaRepository<IdempotencyRecord, Long> {

    /**
     * Find idempotency record by key
     *
     * @param idempotencyKey Unique idempotency key
     * @return Optional containing record if found
     */
    Optional<IdempotencyRecord> findByIdempotencyKey(String idempotencyKey);

    /**
     * Find idempotency records by operation type
     *
     * @param operationType Type of operation (e.g., "PAYMENT", "INVOICE")
     * @return List of records
     */
    List<IdempotencyRecord> findByOperationType(String operationType);

    /**
     * Find idempotency records by process instance ID
     *
     * @param processInstanceId Camunda process instance ID
     * @return List of records
     */
    List<IdempotencyRecord> findByProcessInstanceId(String processInstanceId);

    /**
     * Find idempotency records by status
     *
     * @param status Idempotency status
     * @return List of records
     */
    List<IdempotencyRecord> findByStatus(IdempotencyStatus status);

    /**
     * Find expired idempotency records
     *
     * @param now Current timestamp
     * @return List of expired records
     */
    @Query("SELECT i FROM IdempotencyRecord i WHERE i.expiresAt < :now")
    List<IdempotencyRecord> findExpiredRecords(@Param("now") LocalDateTime now);

    /**
     * Delete expired idempotency records
     *
     * @param now Current timestamp
     * @return Number of deleted records
     */
    @Modifying
    @Query("DELETE FROM IdempotencyRecord i WHERE i.expiresAt < :now")
    int deleteExpiredRecords(@Param("now") LocalDateTime now);

    /**
     * Find stuck processing records (in PROCESSING status for too long)
     *
     * @param threshold Time threshold
     * @return List of stuck records
     */
    @Query("SELECT i FROM IdempotencyRecord i WHERE i.status = 'PROCESSING' AND i.createdAt < :threshold")
    List<IdempotencyRecord> findStuckProcessingRecords(@Param("threshold") LocalDateTime threshold);

    /**
     * Count records by operation type and status
     *
     * @param operationType Operation type
     * @param status Status
     * @return Count of matching records
     */
    long countByOperationTypeAndStatus(String operationType, IdempotencyStatus status);
}
