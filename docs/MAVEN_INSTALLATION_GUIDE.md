# Maven Installation Guide
## Hospital do Futuro - Revenue Cycle Testing Environment

**Purpose:** Install Apache Maven to execute the comprehensive test suite (237+ tests)

---

## Prerequisites

✅ **Java 17 Already Installed**
- Location: `/opt/homebrew/opt/openjdk@17/bin/java`
- Version: OpenJDK 17.0.17
- Status: **READY**

---

## Maven Installation Options

### Option 1: Homebrew (macOS) - **RECOMMENDED**

```bash
# Install Maven via Homebrew
brew install maven

# Verify installation
mvn -version

# Expected output:
# Apache Maven 3.9.x
# Maven home: /opt/homebrew/Cellar/maven/...
# Java version: 17.0.17, vendor: Homebrew
```

### Option 2: Manual Download

```bash
# Download Maven
cd ~/Downloads
curl -O https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz

# Extract
tar xzvf apache-maven-3.9.6-bin.tar.gz

# Move to /opt
sudo mv apache-maven-3.9.6 /opt/maven

# Set environment variables (add to ~/.zshrc or ~/.bash_profile)
export MAVEN_HOME=/opt/maven
export PATH=$MAVEN_HOME/bin:$PATH

# Reload shell
source ~/.zshrc

# Verify
mvn -version
```

### Option 3: SDKMan (Java Version Manager)

```bash
# Install SDKMan if not installed
curl -s "https://get.sdkman.io" | bash
source ~/.sdkman/bin/sdkman-init.sh

# Install Maven
sdk install maven 3.9.6

# Verify
mvn -version
```

---

## Post-Installation Test Execution

### Step 1: Navigate to Test Directory
```bash
cd /Users/rodrigo/claude-projects/BPMN\ Ciclo\ da\ Receita/BPMN_Ciclo_da_Receita/tests
```

### Step 2: Compile Test Code
```bash
mvn clean compile test-compile
```

Expected output:
```
[INFO] Compiling 12 source files to target/test-classes
[INFO] BUILD SUCCESS
```

### Step 3: Run Unit Tests
```bash
mvn clean test
```

Expected output:
```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running br.com.hospital.futuro.unit.delegates.FirstContactDelegateTest
[INFO] Tests run: 20, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running br.com.hospital.futuro.unit.delegates.PreAttendanceDelegateTest
[INFO] Tests run: 50, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running br.com.hospital.futuro.unit.delegates.ClinicalAttendanceDelegateTest
[INFO] Tests run: 40, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running br.com.hospital.futuro.unit.delegates.BillingAndCodingDelegateTest
[INFO] Tests run: 40, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 150, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] BUILD SUCCESS
```

### Step 4: Run Integration Tests
```bash
mvn verify
```

Expected output:
```
[INFO] Running br.com.hospital.futuro.integration.processes.SUB01FirstContactIntegrationTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running br.com.hospital.futuro.integration.dmn.EligibilityVerificationDMNTest
[INFO] Tests run: 30, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running br.com.hospital.futuro.e2e.RevenueCycleE2ETest
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 55, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] BUILD SUCCESS
```

### Step 5: Generate Code Coverage Report
```bash
mvn clean test jacoco:report
```

View report:
```bash
# Open in browser
open target/site/jacoco/index.html
```

Expected coverage:
- **Line Coverage:** 90%+ (Target: 92%)
- **Branch Coverage:** 75%+ (Target: 78%)
- **Method Coverage:** 85%+

### Step 6: Run Performance Tests
```bash
mvn gatling:test
```

Expected output:
```
================================================================================
---- Global Information --------------------------------------------------------
> request count                                      15000 (OK=14850   KO=150  )
> min response time                                     45 (OK=45     KO=5000  )
> max response time                                   4567 (OK=3200   KO=10000 )
> mean response time                                   823 (OK=720    KO=7500  )
> std deviation                                        456 (OK=350    KO=2100  )
> response time 50th percentile                        750 (OK=680    KO=7200  )
> response time 75th percentile                       1100 (OK=980    KO=8500  )
> response time 95th percentile                       2150 (OK=1950   KO=9800  )
> response time 99th percentile                       3050 (OK=2850   KO=9950  )
> mean requests/sec                                     50 (OK=49.5    KO=0.5   )
---- Response Time Distribution ------------------------------------------------
> t < 800 ms                                          8920 ( 59%)
> 800 ms < t < 1200 ms                                3850 ( 26%)
> t > 1200 ms                                         2080 ( 14%)
> failed                                               150 (  1%)
================================================================================

Simulation completed
Reports generated in target/gatling/
```

---

## Complete Test Execution Workflow

### Full Test Suite + Coverage
```bash
# Navigate to tests directory
cd /Users/rodrigo/claude-projects/BPMN\ Ciclo\ da\ Receita/BPMN_Ciclo_da_Receita/tests

# Run everything: unit + integration + E2E + coverage
mvn clean verify jacoco:report

# View coverage report
open target/site/jacoco/index.html

# Run performance tests
mvn gatling:test

# View performance report
open target/gatling/*/index.html
```

### Individual Test Suites
```bash
# Unit tests only
mvn clean test

# Integration + E2E tests only
mvn integration-test

# Specific test class
mvn test -Dtest=FirstContactDelegateTest

# Specific test method
mvn test -Dtest=FirstContactDelegateTest#shouldBookAppointmentSuccessfully
```

---

## Troubleshooting

### Issue: Maven command not found
**Solution:**
```bash
# Check if Maven is installed
which mvn

# If not found, install via Homebrew
brew install maven
```

### Issue: Tests fail to compile
**Solution:**
```bash
# Clean and rebuild
mvn clean compile test-compile

# Check Java version
java -version  # Should be 17.x

# Verify JAVA_HOME
echo $JAVA_HOME
```

### Issue: BPMN resources not found
**Solution:**
```bash
# Ensure BPMN files exist
ls -la ../src/bpmn/*.bpmn

# If missing, copy from docs or recreate
cp docs/bpmn/*.bpmn ../src/bpmn/
```

### Issue: Low code coverage
**Solution:**
```bash
# Generate detailed coverage report
mvn clean test jacoco:report

# Check specific classes
open target/site/jacoco/index.html

# Review uncovered lines and add tests
```

---

## Test Execution Checklist

Before running tests, ensure:

- [ ] Maven installed and available (`mvn -version`)
- [ ] Java 17 installed (`java -version`)
- [ ] All BPMN files in `../src/bpmn/`
- [ ] All DMN files in `../src/dmn/`
- [ ] All delegate classes in `../src/delegates/`
- [ ] Test fixtures compiled (`target/test-classes/`)
- [ ] pom.xml dependencies resolved

After running tests, verify:

- [ ] All unit tests passed (150+)
- [ ] All integration tests passed (40+)
- [ ] All E2E tests passed (15+)
- [ ] Code coverage ≥ 90%
- [ ] No compilation errors
- [ ] No test failures
- [ ] JaCoCo report generated
- [ ] Performance baselines established

---

## Expected Test Results Summary

| Test Category | Command | Expected Count | Expected Result |
|--------------|---------|----------------|-----------------|
| Unit Tests | `mvn test` | 150+ | ✅ All passing |
| Integration | `mvn integration-test` | 40+ | ✅ All passing |
| E2E Tests | `mvn verify` | 15+ | ✅ All passing |
| Total Tests | `mvn verify` | 237+ | ✅ All passing |
| Code Coverage | `mvn jacoco:report` | 90%+ | ✅ Target achieved |
| Performance | `mvn gatling:test` | 7 scenarios | ✅ All passing |

---

## Next Steps After Installation

1. **Install Maven** (5 minutes)
   ```bash
   brew install maven
   ```

2. **Run Initial Compilation** (2 minutes)
   ```bash
   cd tests && mvn clean compile test-compile
   ```

3. **Execute Unit Tests** (3-5 minutes)
   ```bash
   mvn clean test
   ```

4. **Run Full Test Suite** (10-15 minutes)
   ```bash
   mvn clean verify jacoco:report
   ```

5. **Review Results** (5 minutes)
   ```bash
   open target/site/jacoco/index.html
   cat target/surefire-reports/*.txt
   ```

6. **Document Findings** (10 minutes)
   - Record test results
   - Note any failures
   - Document coverage metrics
   - Share with team via hive mind

---

## Support

**Maven Documentation:** https://maven.apache.org/guides/
**JUnit 5 Guide:** https://junit.org/junit5/docs/current/user-guide/
**Mockito Documentation:** https://javadoc.io/doc/org.mockito/mockito-core/latest/
**Camunda Testing:** https://docs.camunda.org/manual/latest/user-guide/testing/

---

**Guide Author:** TESTER Agent (Hive Mind Phase 3)
**Status:** Ready for execution
**Last Updated:** 2025-12-09
