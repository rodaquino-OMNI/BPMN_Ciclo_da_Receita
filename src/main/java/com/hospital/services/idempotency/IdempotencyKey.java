package com.hospital.services.idempotency;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * JPA Entity representing an idempotency key for preventing duplicate operations.
 *
 * <p>This entity stores the result of operations to ensure that the same operation
 * is not executed multiple times. It uses a composite unique constraint on
 * operationType and operationKey to enforce idempotency.</p>
 *
 * @author Hospital Revenue Cycle System
 * @version 1.0
 */
@Entity
@Table(
    name = "idempotency_keys",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_operation_type_key",
            columnNames = {"operation_type", "operation_key"}
        )
    },
    indexes = {
        @Index(name = "idx_expires_at", columnList = "expires_at"),
        @Index(name = "idx_process_instance", columnList = "process_instance_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "created_at")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"operationType", "operationKey"})
public class IdempotencyKey {

    /**
     * Primary key, auto-generated.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Type of operation (e.g., "REGISTER_PATIENT", "CREATE_INVOICE").
     * Used to namespace different types of idempotent operations.
     */
    @Column(name = "operation_type", nullable = false, length = 100)
    private String operationType;

    /**
     * Unique key for this specific operation instance.
     * Generated from operation parameters to ensure deterministic identification.
     */
    @Column(name = "operation_key", nullable = false, length = 255)
    private String operationKey;

    /**
     * Camunda process instance ID associated with this operation.
     */
    @Column(name = "process_instance_id", length = 64)
    private String processInstanceId;

    /**
     * Camunda execution ID when the operation was performed.
     */
    @Column(name = "execution_id", length = 64)
    private String executionId;

    /**
     * Serialized result of the operation (JSON format).
     * Stored to return the same result on duplicate requests.
     */
    @Column(name = "result", columnDefinition = "TEXT")
    private String result;

    /**
     * Current status of the idempotency key.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private IdempotencyStatus status = IdempotencyStatus.PENDING;

    /**
     * Timestamp when the idempotency key was created.
     */
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Timestamp when this idempotency key expires and can be cleaned up.
     * Default is 24 hours from creation.
     */
    @Column(name = "expires_at", nullable = false)
    @Builder.Default
    private LocalDateTime expiresAt = LocalDateTime.now().plusDays(1);

    /**
     * Version field for optimistic locking to handle concurrent updates.
     */
    @Version
    @Column(name = "version")
    private Long version;

    /**
     * Status enumeration for idempotency keys.
     */
    public enum IdempotencyStatus {
        /**
         * Operation is pending execution.
         */
        PENDING,

        /**
         * Operation is currently being executed.
         */
        PROCESSING,

        /**
         * Operation completed successfully.
         */
        COMPLETED,

        /**
         * Operation failed.
         */
        FAILED
    }

    /**
     * Marks this idempotency key as processing.
     */
    public void markProcessing() {
        this.status = IdempotencyStatus.PROCESSING;
    }

    /**
     * Marks this idempotency key as completed with a result.
     *
     * @param result the serialized result to store
     */
    public void markCompleted(String result) {
        this.status = IdempotencyStatus.COMPLETED;
        this.result = result;
    }

    /**
     * Marks this idempotency key as failed.
     */
    public void markFailed() {
        this.status = IdempotencyStatus.FAILED;
    }

    /**
     * Checks if this idempotency key has expired.
     *
     * @return true if expired, false otherwise
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Checks if the operation is already completed.
     *
     * @return true if completed, false otherwise
     */
    public boolean isCompleted() {
        return status == IdempotencyStatus.COMPLETED;
    }
}
