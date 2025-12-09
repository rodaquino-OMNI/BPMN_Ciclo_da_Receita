# API Documentation Overview

## Introduction

This section provides comprehensive API documentation for all delegates, service tasks, and external integrations in the Hospital Revenue Cycle BPMN implementation.

## API Architecture

### Service Integration Layers

```
┌─────────────────────────────────────────────────────────────┐
│                    BPMN Process Layer                        │
│              (Camunda Process Definitions)                   │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│                  Delegate Layer                              │
│         (Java Delegates & External Task Workers)             │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│                  Service Layer                               │
│            (Business Logic & Orchestration)                  │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│               Integration Layer                              │
│       (External APIs, RPA, AI/ML, Databases)                │
└─────────────────────────────────────────────────────────────┘
```

## API Categories

### 1. Java Delegates
Service tasks implemented as Java Delegates directly embedded in Camunda.

**Location**: `/src/delegates/`
**Pattern**: Synchronous execution
**Use Cases**: Simple transformations, business rules, validations

### 2. External Task Workers
Asynchronous workers that pull tasks from Camunda.

**Location**: `/src/tasks/`
**Pattern**: Asynchronous execution with retries
**Use Cases**: Long-running operations, external integrations, RPA

### 3. REST APIs
RESTful APIs for external system integration.

**Base URL**: `https://api.hospital.com/v1`
**Authentication**: OAuth 2.0 + JWT
**Format**: JSON

### 4. RPA Connectors
Robotic Process Automation integration points.

**Platform**: IBM RPA / UiPath
**Execution**: Asynchronous with callback
**Use Cases**: Portal automation, document processing

### 5. AI/ML Services
Artificial Intelligence and Machine Learning integrations.

**Platform**: OpenAI API, Custom ML Models
**Execution**: REST API calls
**Use Cases**: Coding suggestions, appeal generation, predictions

## Delegate Reference

### Common Delegate Base Class

All delegates extend the base delegate class:

```java
public abstract class BaseDelegate implements JavaDelegate {

    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        try {
            LOGGER.info("Executing delegate: {}", this.getClass().getSimpleName());
            executeBusinessLogic(execution);
            LOGGER.info("Delegate execution completed: {}", this.getClass().getSimpleName());
        } catch (Exception e) {
            LOGGER.error("Delegate execution failed: {}", this.getClass().getSimpleName(), e);
            throw new BpmnError("DELEGATE_ERROR", e.getMessage());
        }
    }

    protected abstract void executeBusinessLogic(DelegateExecution execution) throws Exception;

    protected <T> T getVariable(DelegateExecution execution, String name, Class<T> type) {
        return execution.getVariable(name, type);
    }

    protected void setVariable(DelegateExecution execution, String name, Object value) {
        execution.setVariable(name, value);
    }
}
```

## Delegate Naming Conventions

| Pattern | Example | Purpose |
|---------|---------|---------|
| `Validate*Delegate` | `ValidateEligibilityDelegate` | Data validation |
| `Calculate*Delegate` | `CalculateCopayDelegate` | Calculations |
| `Generate*Delegate` | `GenerateTISSDelegate` | Document generation |
| `Send*Delegate` | `SendNotificationDelegate` | External communication |
| `Process*Delegate` | `ProcessPaymentDelegate` | Data processing |
| `Check*Delegate` | `CheckAvailabilityDelegate` | Status checks |

## External Task Workers

### Worker Configuration

```yaml
camunda:
  bpm:
    client:
      base-url: http://camunda-engine:8080/engine-rest
      worker-id: ${HOSTNAME}
      max-tasks: 10
      lock-duration: 60000
      async-response-timeout: 10000

workers:
  - topic: rpa-eligibility-check
    type: rpa
    lock-duration: 300000
    retries: 3

  - topic: ai-coding-suggestion
    type: ai
    lock-duration: 120000
    retries: 2

  - topic: tasy-integration
    type: integration
    lock-duration: 60000
    retries: 3
```

### Worker Pattern

```java
@Component
public class RPAEligibilityWorker {

    @ExternalTaskSubscription("rpa-eligibility-check")
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        try {
            // Get variables
            String patientCPF = externalTask.getVariable("patientCPF");
            String insuranceId = externalTask.getVariable("insuranceId");

            // Execute RPA
            RPAResult result = rpaService.checkEligibility(patientCPF, insuranceId);

            // Set output variables
            Map<String, Object> variables = new HashMap<>();
            variables.put("isEligible", result.isEligible());
            variables.put("planDetails", result.getPlanDetails());

            // Complete task
            externalTaskService.complete(externalTask, variables);

        } catch (Exception e) {
            // Handle failure
            externalTaskService.handleFailure(externalTask,
                "RPA execution failed",
                e.getMessage(),
                externalTask.getRetries() - 1,
                10000);
        }
    }
}
```

## REST API Endpoints

### Base Configuration

```yaml
server:
  port: 8080
  servlet:
    context-path: /api/v1

spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://auth.hospital.com/realms/hospital
```

### Authentication

All API requests require JWT authentication:

```bash
curl -X POST https://api.hospital.com/v1/processes/start \
  -H "Authorization: Bearer ${JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "businessKey": "PAT-12345",
    "variables": {
      "patientId": {"value": "12345", "type": "String"},
      "contactChannel": {"value": "WHATSAPP", "type": "String"}
    }
  }'
```

### Standard Response Format

```json
{
  "success": true,
  "data": {
    "processInstanceId": "proc-inst-123",
    "businessKey": "PAT-12345"
  },
  "message": "Process started successfully",
  "timestamp": "2025-12-08T10:30:00Z"
}
```

### Error Response Format

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Patient ID is required",
    "details": {
      "field": "patientId",
      "constraint": "NOT_NULL"
    }
  },
  "timestamp": "2025-12-08T10:30:00Z"
}
```

## Integration Points

### TASY ERP Integration

**Base URL**: `http://tasy-api.hospital.local/api/v2`
**Authentication**: API Key + HMAC
**Protocol**: REST/JSON

**Key Endpoints**:
- `POST /patients` - Create patient
- `GET /patients/{id}` - Get patient details
- `POST /appointments` - Create appointment
- `GET /appointments/availability` - Check availability
- `POST /admissions` - Create admission
- `POST /billing` - Create bill

### Insurance Portal Integration (RPA)

**Platform**: IBM RPA
**Execution**: Asynchronous
**Retry**: 3 attempts with exponential backoff

**RPA Bots**:
- `eligibility-checker-bot` - Check eligibility
- `authorization-submitter-bot` - Submit authorizations
- `denial-scraper-bot` - Scrape denial information
- `portal-uploader-bot` - Upload billing files

### AI/ML Services

**Provider**: OpenAI API + Custom Models
**Base URL**: `https://ai-services.hospital.com/v1`
**Authentication**: API Key

**Services**:
- `POST /coding/suggest` - TUSS code suggestions
- `POST /appeals/generate` - Generate appeal text
- `POST /anomaly/detect` - Anomaly detection
- `POST /prediction/revenue` - Revenue prediction

## Error Handling

### Error Codes

| Code | Description | Action |
|------|-------------|--------|
| `BPMN_ERROR` | Business error | Handle with error boundary event |
| `VALIDATION_ERROR` | Input validation failed | Return to user with message |
| `INTEGRATION_ERROR` | External service failure | Retry with backoff |
| `TIMEOUT_ERROR` | Operation timeout | Retry or escalate |
| `AUTH_ERROR` | Authentication failed | Refresh token and retry |

### Retry Strategy

```java
@Retryable(
    value = {IntegrationException.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
)
public Result callExternalService(Request request) {
    // Service call implementation
}
```

### Circuit Breaker

```java
@CircuitBreaker(
    name = "tasy-integration",
    fallbackMethod = "fallbackMethod"
)
public Response callTasyAPI(Request request) {
    // TASY API call
}

public Response fallbackMethod(Request request, Exception ex) {
    // Fallback logic
}
```

## Performance Considerations

### Async Execution

Critical for long-running operations:

```xml
<bpmn:serviceTask id="Task_LLM_Appeal"
                  name="Generate Appeal"
                  camunda:asyncBefore="true"
                  camunda:delegateExpression="${generateAppealDelegate}">
</bpmn:serviceTask>
```

### Connection Pooling

```yaml
datasource:
  hikari:
    maximum-pool-size: 20
    minimum-idle: 5
    connection-timeout: 30000
    idle-timeout: 600000
    max-lifetime: 1800000
```

### Caching

```java
@Cacheable(value = "insurance-plans", key = "#insuranceId")
public InsurancePlan getInsurancePlan(String insuranceId) {
    return insuranceRepository.findById(insuranceId);
}
```

## Security

### API Key Management

```java
@Configuration
public class SecurityConfig {

    @Value("${tasy.api.key}")
    private String tasyApiKey;

    @Bean
    public RestTemplate tasyRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().set("X-API-Key", tasyApiKey);
            return execution.execute(request, body);
        });
        return restTemplate;
    }
}
```

### Data Encryption

All sensitive data is encrypted:

```java
public class EncryptionService {

    @Autowired
    private TextEncryptor encryptor;

    public String encrypt(String plainText) {
        return encryptor.encrypt(plainText);
    }

    public String decrypt(String cipherText) {
        return encryptor.decrypt(cipherText);
    }
}
```

## Monitoring and Observability

### Metrics

Expose metrics for monitoring:

```java
@Component
public class DelegateMetrics {

    private final Counter delegateExecutions;
    private final Timer delegateExecutionTime;

    public DelegateMetrics(MeterRegistry registry) {
        this.delegateExecutions = Counter.builder("delegate.executions")
            .tag("type", "service-task")
            .register(registry);

        this.delegateExecutionTime = Timer.builder("delegate.execution.time")
            .register(registry);
    }
}
```

### Logging

Structured logging for all API calls:

```java
LOGGER.info("Executing TASY API call",
    kv("endpoint", endpoint),
    kv("patientId", patientId),
    kv("requestId", requestId)
);
```

### Tracing

Distributed tracing with correlation IDs:

```java
@Component
public class CorrelationIdInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                       ClientHttpRequestExecution execution) {
        String correlationId = MDC.get("correlationId");
        if (correlationId != null) {
            request.getHeaders().set("X-Correlation-ID", correlationId);
        }
        return execution.execute(request, body);
    }
}
```

## Testing

### Unit Testing Delegates

```java
@ExtendWith(MockitoExtension.class)
class ValidateEligibilityDelegateTest {

    @Mock
    private DelegateExecution execution;

    @InjectMocks
    private ValidateEligibilityDelegate delegate;

    @Test
    void shouldValidateEligibility() throws Exception {
        when(execution.getVariable("patientCPF")).thenReturn("12345678900");
        when(execution.getVariable("insuranceId")).thenReturn("INS-001");

        delegate.execute(execution);

        verify(execution).setVariable(eq("isEligible"), eq(true));
    }
}
```

### Integration Testing

```java
@SpringBootTest
@ExtendWith(CamundaExtension.class)
class FirstContactProcessIT {

    @Test
    @Deployment(resources = "SUB_01_First_Contact.bpmn")
    void shouldCompleteFirstContactProcess() {
        ProcessInstance processInstance = runtimeService()
            .startProcessInstanceByKey("Process_SUB_01_First_Contact",
                Variables.putValue("patientId", "PAT-123"));

        assertThat(processInstance).isStarted();
        assertThat(processInstance).isWaitingAt("Task_Capture_Data");

        complete(task(), withVariables("patientName", "John Doe"));

        assertThat(processInstance).isEnded();
    }
}
```

## API Versioning

All APIs follow semantic versioning:

- **v1**: Current stable version
- **v2**: Next major version (backward incompatible)
- **v1.1**: Minor updates (backward compatible)

URL pattern: `/api/v{major}/resource`

## Rate Limiting

API calls are rate-limited:

```yaml
rate-limiting:
  enabled: true
  limits:
    - key: "api-key"
      rate: 1000
      duration: 1h
    - key: "ip-address"
      rate: 100
      duration: 1m
```

## Documentation Standards

All APIs must include:
- OpenAPI 3.0 specification
- Request/response examples
- Error codes and descriptions
- Authentication requirements
- Rate limits

See [OpenAPI Specification](./openapi.yaml) for complete API reference.

---

**Next**: [Delegate Reference](./01_Delegates.md)

**Last Updated**: 2025-12-08
