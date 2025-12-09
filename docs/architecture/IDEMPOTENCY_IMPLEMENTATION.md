# Payment Idempotency Implementation

## Overview

This document describes the idempotency implementation for the ProcessPatientPaymentDelegate to prevent duplicate payment processing during Camunda workflow retries.

## Architecture

### Components

1. **IdempotencyService** - Core service managing idempotency records
2. **IdempotencyKeyGenerator** - Generates deterministic keys from operation parameters
3. **IdempotencyRecord** - JPA entity tracking operation execution state
4. **IdempotencyRepository** - Data access layer for idempotency records
5. **IdempotencyException** - Custom exception for idempotency violations
6. **IdempotencyCleanupScheduler** - Scheduled task to remove expired records

### Key Generation Strategy

Idempotency keys are generated using SHA-256 hash of:
- Operation type (e.g., "PAYMENT")
- Process instance ID
- Patient ID
- Payment amount
- Payment method

This ensures the same inputs always produce the same key, making the operation deterministic.

### Operation States

1. **PROCESSING** - Operation currently executing
2. **COMPLETED** - Operation finished successfully (result cached)
3. **FAILED** - Operation failed (allows retry with limit)

## ProcessPatientPaymentDelegate Refactoring

### Before (No Idempotency)

```java
public class ProcessPatientPaymentDelegate implements JavaDelegate {
    public void execute(DelegateExecution execution) {
        // Direct payment processing - vulnerable to duplicates
        PaymentResult result = processPayment(...);
        execution.setVariable("paymentTransactionId", result.transactionId);
    }
}
```

### After (With Idempotency)

```java
@Component
@Named("processPatientPaymentDelegate")
public class ProcessPatientPaymentDelegate implements JavaDelegate {

    @Autowired
    private IdempotencyService idempotencyService;

    @Autowired
    private IdempotencyKeyGenerator idempotencyKeyGenerator;

    public void execute(DelegateExecution execution) {
        // Generate deterministic key
        String idempotencyKey = idempotencyKeyGenerator.generatePaymentKey(
            processInstanceId, patientId, paymentAmount, paymentMethod
        );

        // Execute with idempotency protection
        PaymentResult result = idempotencyService.executeIdempotent(
            "PAYMENT",
            idempotencyKey,
            () -> processPaymentInternal(...)
        );

        setOutputVariables(execution, result);
    }
}
```

## Transaction Management

### Idempotency Check Transaction

```java
@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
public <T> T executeIdempotent(String operationType, String idempotencyKey, Supplier<T> operation)
```

- **REQUIRES_NEW**: Creates separate transaction for idempotency check
- **READ_COMMITTED**: Prevents dirty reads while allowing concurrent operations

### Payment Processing Transaction

The actual payment processing inherits the calling transaction context, but idempotency check happens in separate transaction to ensure atomicity of record creation.

## Error Handling

### Idempotency Violations

1. **Operation Already Completed**
   - Returns cached result from `response_payload`
   - Logs idempotency hit
   - No duplicate processing occurs

2. **Operation In Progress**
   - Waits up to 30 seconds for completion
   - Throws `PAYMENT_IN_PROGRESS` BpmnError if timeout
   - Allows Camunda retry with backoff

3. **Operation Previously Failed**
   - Allows retry up to 3 attempts
   - Increments `retry_count`
   - Throws error if max retries exceeded

### Exception Mapping to BPMN Errors

```java
PAYMENT_IN_PROGRESS      → Retry with backoff
PAYMENT_VALIDATION_ERROR → Handle invalid input
IDEMPOTENCY_ERROR        → Log and alert
PAYMENT_PROCESSING_ERROR → General error handling
```

## Database Schema

```sql
CREATE TABLE idempotency_records (
    id BIGSERIAL PRIMARY KEY,
    idempotency_key VARCHAR(255) UNIQUE NOT NULL,
    operation_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    request_payload TEXT,
    response_payload TEXT,          -- Cached result (JSON)
    error_message TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,  -- Auto-cleanup after 24 hours
    process_instance_id VARCHAR(100),
    retry_count INTEGER DEFAULT 0
);
```

## Logging Strategy

### Comprehensive Logging Points

1. **Key Generation**
   ```
   INFO: Generated idempotency key: {key} for payment processing
   ```

2. **Idempotency Hit**
   ```
   INFO: Idempotency hit - Payment already processed for key: {key}
   INFO: Returning cached payment result - Transaction: {txId}
   ```

3. **New Execution**
   ```
   INFO: Executing new payment with idempotency protection - Key: {key}
   INFO: Executing internal payment processing - Patient: {id}, Amount: {amt}
   ```

4. **Completion**
   ```
   INFO: Payment execution completed - Transaction: {txId}
   INFO: Payment processed successfully - Transaction: {txId}, Success: {status}, Remaining: {balance}
   ```

5. **Errors**
   ```
   ERROR: Idempotency violation detected - Key: {key}, Status: {status}
   ERROR: Idempotency error in payment processing - Key: {key}, Status: {status}, Message: {msg}
   ```

## Cleanup Strategy

### Scheduled Cleanup

1. **Daily Cleanup** (2:00 AM)
   - Removes records older than 24 hours
   - Primary cleanup mechanism

2. **Frequent Cleanup** (Every 6 hours)
   - Backup cleanup for high-volume systems
   - Prevents table bloat

### Manual Cleanup

```java
int deletedCount = idempotencyService.cleanupExpiredRecords();
```

## Performance Considerations

### Indexes

```sql
CREATE UNIQUE INDEX idx_idempotency_key ON idempotency_records(idempotency_key);
CREATE INDEX idx_operation_type ON idempotency_records(operation_type);
CREATE INDEX idx_created_at ON idempotency_records(created_at);
CREATE INDEX idx_expires_at ON idempotency_records(expires_at);
```

### Caching

- Result caching via `response_payload` (JSON)
- Eliminates need for re-execution
- Fast lookup by idempotency_key (unique index)

## Testing Scenarios

### Unit Tests Needed

1. First execution creates idempotency record
2. Retry returns cached result
3. Failed operation allows retry
4. Max retries prevents infinite loops
5. Concurrent executions handle PROCESSING state
6. Expired records cleanup works

### Integration Tests Needed

1. End-to-end payment with Camunda retry
2. Multiple concurrent payment attempts
3. Transaction rollback scenarios
4. Database constraint violations

## Configuration

### Spring Configuration

```java
@EnableTransactionManagement
@EnableScheduling
public class ApplicationConfig {
    // Transaction manager must support REQUIRES_NEW
    // Scheduling must be enabled for cleanup tasks
}
```

### Application Properties

```properties
# Enable scheduling
spring.task.scheduling.pool.size=2

# Transaction settings
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
```

## Monitoring & Observability

### Metrics to Track

1. Idempotency hit rate
2. Average operation duration
3. Failed operation count
4. Retry count distribution
5. Cleanup job execution time

### Log Queries

```sql
-- Find stuck PROCESSING records
SELECT * FROM idempotency_records
WHERE status = 'PROCESSING'
  AND created_at < NOW() - INTERVAL '5 minutes';

-- Idempotency hit rate by operation type
SELECT operation_type, status, COUNT(*)
FROM idempotency_records
GROUP BY operation_type, status;
```

## Best Practices

1. **Always validate inputs** before generating idempotency key
2. **Use deterministic keys** - same inputs must produce same key
3. **Set appropriate TTL** - 24 hours default for payments
4. **Log idempotency decisions** for audit trail
5. **Handle concurrent access** with proper isolation levels
6. **Test retry scenarios** thoroughly
7. **Monitor cleanup jobs** to prevent table bloat

## Security Considerations

1. **Key generation includes sensitive data** (patient ID, amounts)
2. **SHA-256 hashing** prevents key guessing
3. **Response payload contains PII** - ensure database encryption
4. **Access control** on idempotency_records table

## Future Enhancements

1. Distributed caching (Redis) for high-volume scenarios
2. Metrics integration (Prometheus/Grafana)
3. Idempotency for other critical operations (invoice generation)
4. Configurable TTL per operation type
5. Idempotency key versioning for schema changes

## References

- Idempotency patterns: https://stripe.com/docs/api/idempotent_requests
- Transaction isolation levels: https://www.postgresql.org/docs/current/transaction-iso.html
- Camunda retry mechanisms: https://docs.camunda.org/manual/latest/user-guide/process-engine/transactions-in-processes/
