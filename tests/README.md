# Revenue Cycle Test Suite

Comprehensive testing suite for Hospital do Futuro's BPMN Revenue Cycle processes.

## 📁 Test Structure

```
tests/
├── unit/                      # Unit tests (fast, isolated)
│   ├── delegates/            # Java delegate tests
│   ├── services/             # Service layer tests
│   └── utils/                # Utility class tests
├── integration/              # Integration tests
│   ├── processes/            # BPMN process tests
│   └── flows/                # End-to-end flow tests
├── e2e/                      # End-to-end tests
├── fixtures/                 # Test data builders
├── performance/              # Performance & load tests
├── deployment/               # Camunda deployment tests
└── reports/                  # Test coverage reports
```

## 🧪 Test Types

### 1. Unit Tests
- **Location**: `tests/unit/`
- **Purpose**: Test individual components in isolation
- **Framework**: JUnit 5 + Mockito
- **Coverage Target**: >80%
- **Execution**: `mvn test`

### 2. Integration Tests
- **Location**: `tests/integration/`
- **Purpose**: Test BPMN process execution with Camunda
- **Framework**: JUnit 5 + Camunda BPM Assert
- **Database**: H2 in-memory or Testcontainers PostgreSQL
- **Execution**: `mvn verify`

### 3. End-to-End Tests
- **Location**: `tests/e2e/`
- **Purpose**: Test complete revenue cycle scenarios
- **Framework**: JUnit 5 + REST Assured
- **Execution**: `mvn verify -Dtest.profile=e2e`

### 4. Performance Tests
- **Location**: `tests/performance/`
- **Purpose**: Load testing and performance benchmarks
- **Framework**: Gatling
- **Execution**: `mvn gatling:test`

## 🚀 Quick Start

### Run All Unit Tests
```bash
mvn test
```

### Run Integration Tests
```bash
mvn verify
```

### Run Specific Test Class
```bash
mvn test -Dtest=FirstContactDelegateTest
```

### Run with Coverage Report
```bash
mvn clean verify
# Report generated at: target/site/jacoco/index.html
```

### Run Performance Tests
```bash
mvn gatling:test
```

## 📊 Test Coverage

### Coverage Requirements
- **Statements**: >80%
- **Branches**: >75%
- **Functions**: >80%
- **Lines**: >80%

### Viewing Coverage Reports
```bash
mvn clean verify
open target/site/jacoco/index.html  # macOS
xdg-open target/site/jacoco/index.html  # Linux
start target/site/jacoco/index.html  # Windows
```

## 🧩 Test Data Fixtures

Fixtures provide consistent test data for all scenarios:

```java
// Patient fixtures
Patient patient = PatientFixtures.defaultPatient();
Patient insurancePatient = PatientFixtures.withInsurance("UNIMED");

// Insurance fixtures
Insurance insurance = InsuranceFixtures.unimed();
AuthorizationRequest authRequest = AuthorizationFixtures.surgeryRequest();

// Clinical fixtures
ClinicalData clinicalData = ClinicalFixtures.surgicalProcedure();
Prescription prescription = ClinicalFixtures.prescription();

// Financial fixtures
BillingData billing = BillingFixtures.completedAccount();
Payment payment = PaymentFixtures.fullPayment();
```

## 🎯 Test Scenarios

### Happy Path Scenarios
1. **Complete Revenue Cycle**: Patient registration → Discharge → Payment
2. **With Insurance**: Authorization → Clinical care → Billing → Collection
3. **Without Insurance**: Direct admission → Cash payment

### Error Scenarios
1. **Authorization Denial**: Test appeal workflow
2. **Billing Rejection**: Test glosa management
3. **Integration Failures**: Test retry mechanisms
4. **Timeout Scenarios**: Test timer events

### Edge Cases
1. **Concurrent Processes**: Multiple patients simultaneously
2. **High Volume**: 1000+ transactions
3. **Boundary Values**: Maximum lengths, edge dates
4. **Data Validation**: Invalid inputs, missing data

## 🔍 Testing Best Practices

### 1. Test Naming Convention
```java
@Test
void shouldCreatePatientWhenValidDataProvided() { }

@Test
void shouldThrowExceptionWhenPatientCPFIsInvalid() { }

@Test
void shouldRetrySubmissionOnTemporaryFailure() { }
```

### 2. Arrange-Act-Assert Pattern
```java
@Test
void shouldCalculateCoPayCorrectly() {
    // Arrange
    Insurance insurance = InsuranceFixtures.withCoPayPercent(20.0);
    BigDecimal totalAmount = new BigDecimal("1000.00");

    // Act
    BigDecimal coPayAmount = coPayCalculator.calculate(insurance, totalAmount);

    // Assert
    assertThat(coPayAmount).isEqualByComparingTo("200.00");
}
```

### 3. Use Test Data Builders
```java
Patient patient = PatientBuilder.builder()
    .withName("João Silva")
    .withCPF("123.456.789-00")
    .withInsurance(insurance)
    .build();
```

### 4. Mock External Dependencies
```java
@Mock
private TASYApiClient tasyClient;

@Mock
private InsurancePortalRPA rpaBot;

@Test
void shouldCallTASYApiWhenCreatingPatient() {
    when(tasyClient.createPatient(any())).thenReturn("PAT123");

    String patientId = patientService.createPatient(patientData);

    verify(tasyClient).createPatient(patientData);
    assertThat(patientId).isEqualTo("PAT123");
}
```

## 📋 Checklist for New Tests

- [ ] Test class follows naming convention (`*Test.java` for unit, `*IntegrationTest.java` for integration)
- [ ] All test methods have descriptive names (`should...When...`)
- [ ] Tests are independent (can run in any order)
- [ ] Tests clean up resources (use `@AfterEach` if needed)
- [ ] Tests use fixtures for data
- [ ] External dependencies are mocked
- [ ] Assertions are clear and specific
- [ ] Edge cases are covered
- [ ] Error scenarios are tested
- [ ] Tests run quickly (<100ms for unit tests)

## 🐛 Debugging Tests

### Enable Detailed Logging
```xml
<!-- src/test/resources/logback-test.xml -->
<logger name="org.camunda.bpm.engine" level="DEBUG"/>
<logger name="br.com.hospital.futuro" level="DEBUG"/>
```

### Run Single Test in Debug Mode
```bash
mvn test -Dtest=FirstContactDelegateTest#shouldValidatePatientCPF -Dmaven.surefire.debug
```

### View Process Instance History
```java
@Test
void shouldCompleteProcess() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("Process_SUB_01_First_Contact");

    complete(task(), withVariables("patientCPF", "12345678900"));

    // Debug: Print all activities
    List<HistoricActivityInstance> activities = historyService
        .createHistoricActivityInstanceQuery()
        .processInstanceId(pi.getId())
        .list();

    activities.forEach(activity ->
        System.out.println(activity.getActivityId() + " - " + activity.getActivityName())
    );
}
```

## 📈 Continuous Integration

Tests are automatically executed in CI/CD pipeline:

```yaml
# .github/workflows/tests.yml
name: Test Suite
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Run tests
        run: mvn clean verify
      - name: Upload coverage
        uses: codecov/codecov-action@v3
```

## 🔧 Troubleshooting

### Tests Fail with "Process not deployed"
```java
// Make sure BPMN file is in src/test/resources or use @Deployment
@Deployment(resources = "bpmn/SUB_01_First_Contact.bpmn")
```

### Tests Timeout
```java
// Increase timeout for long-running tests
@Test
@Timeout(value = 30, unit = TimeUnit.SECONDS)
void longRunningTest() { }
```

### Database Connection Issues
```properties
# src/test/resources/camunda.cfg.xml
<property name="jdbcUrl" value="jdbc:h2:mem:camunda;DB_CLOSE_DELAY=-1"/>
<property name="databaseSchemaUpdate" value="true"/>
```

## 📚 Resources

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Camunda BPM Assert](https://github.com/camunda/camunda-bpm-assert)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [Gatling Documentation](https://gatling.io/docs/gatling/)

## 👥 Contributing

When adding new tests:
1. Follow existing patterns and conventions
2. Update this README if adding new test types
3. Ensure all tests pass before committing
4. Maintain >80% code coverage
5. Document complex test scenarios

---

**Version**: 1.0.0
**Last Updated**: December 2025
**Maintained by**: QA Team - Hospital do Futuro
