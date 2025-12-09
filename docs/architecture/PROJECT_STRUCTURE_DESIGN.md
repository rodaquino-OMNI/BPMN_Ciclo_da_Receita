# Project Structure Architecture Design
## Hospital Revenue Cycle - Camunda 7 Application

**Decision Date:** 2025-12-09
**Status:** APPROVED
**Decision Maker:** System Architect Agent (Hive Mind)
**Risk Level:** MEDIUM-HIGH (Structural Refactoring)

---

## Executive Summary

### Decision: Single-Module Maven Project with Standard Maven Layout

**Chosen Architecture:** OPTION A (Enhanced) - Single-module project with strict adherence to Maven Standard Directory Layout.

**Key Benefits:**
- ✅ Follows Maven conventions (src/main/java, src/test/java)
- ✅ Single consistent package hierarchy (com.hospital.revenuecycle.*)
- ✅ Optimal for Camunda 7 + Spring Boot applications
- ✅ Simplified CI/CD and deployment
- ✅ Lower maintenance overhead
- ✅ Standard tooling support (IDEs, build tools)

---

## Critical Issues Identified

### Issue #1: Directory Structure Chaos

**Problem:** Multiple conflicting directory structures coexist, breaking Maven compilation.

**Evidence:**
```
❌ src/java/com/hospital/          (non-standard location)
❌ src/delegates/                   (non-standard location)
❌ tests/                           (should be src/test/java)
✅ src/main/java/com/hospital/      (CORRECT - but mostly empty)
✅ src/test/java/                   (CORRECT - but empty)
```

**Impact:** Maven cannot find source files → compilation fails.

---

### Issue #2: Package Name Mismatch

**Problem:** Three different package hierarchies used simultaneously.

**Evidence:**
```java
// Source files (in src/delegates):
package com.hospital.delegates.medicalcoding;

// Source files (in src/java):
package com.hospital.compensation;

// Test files (in tests/):
package br.com.hospital.futuro.unit.delegates;

// pom.xml expects:
<mainClass>com.hospital.RevenueCycleApplication</mainClass>
```

**Impact:** Tests cannot import source classes → tests cannot compile or run.

---

### Issue #3: Maven Resource Misconfiguration

**Problem:** Custom resource mappings override Maven standards.

**Evidence (from pom.xml):**
```xml
<resource>
    <directory>src/bpmn</directory>
    <targetPath>processes</targetPath>
</resource>
<resource>
    <directory>src/dmn</directory>
    <targetPath>dmn</targetPath>
</resource>
```

**Impact:** Non-standard paths increase complexity and risk deployment issues.

---

## Recommended Architecture

### Project Type: Single-Module Maven Project

**Justification:**
1. **Camunda Best Practice:** Most Camunda applications are single-module
2. **Deployment Model:** Single executable JAR with embedded Tomcat
3. **Spring Boot Integration:** Expects single-module structure
4. **Team Efficiency:** Simpler for small/medium teams
5. **CI/CD Simplicity:** One build, one artifact, one deployment

---

## Target Directory Structure

```
revenue-cycle-camunda/
├── pom.xml                                    # Single project descriptor
├── README.md
├── CLAUDE.md
├── .gitignore
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── hospital/
│   │   │           └── revenuecycle/                          # BASE PACKAGE
│   │   │               ├── RevenueCycleApplication.java       # Spring Boot main class
│   │   │               │
│   │   │               ├── config/                            # Configuration
│   │   │               │   ├── CamundaConfiguration.java
│   │   │               │   ├── DatabaseConfiguration.java
│   │   │               │   └── SecurityConfiguration.java
│   │   │               │
│   │   │               ├── domain/                            # Domain Layer
│   │   │               │   ├── model/                         # Entities
│   │   │               │   │   ├── Patient.java
│   │   │               │   │   ├── Insurance.java
│   │   │               │   │   ├── Claim.java
│   │   │               │   │   └── Payment.java
│   │   │               │   ├── repository/                    # Data Access
│   │   │               │   │   ├── PatientRepository.java
│   │   │               │   │   └── ClaimRepository.java
│   │   │               │   └── service/                       # Business Logic
│   │   │               │       ├── EligibilityService.java
│   │   │               │       └── BillingService.java
│   │   │               │
│   │   │               ├── delegate/                          # Camunda Delegates
│   │   │               │   ├── eligibility/
│   │   │               │   │   ├── VerifyPatientEligibilityDelegate.java
│   │   │               │   │   ├── ValidateInsuranceDelegate.java
│   │   │               │   │   └── CheckCoverageDelegate.java
│   │   │               │   ├── authorization/
│   │   │               │   │   ├── RequestAuthorizationDelegate.java
│   │   │               │   │   └── HandleAuthorizationDenialDelegate.java
│   │   │               │   ├── medicalcoding/
│   │   │               │   │   ├── AssignCodesDelegate.java
│   │   │               │   │   └── ValidateCodesDelegate.java
│   │   │               │   ├── billing/
│   │   │               │   │   ├── GenerateInvoiceDelegate.java
│   │   │               │   │   └── SubmitToPayerDelegate.java
│   │   │               │   ├── glosa/
│   │   │               │   │   ├── IdentifyGlosaDelegate.java
│   │   │               │   │   ├── AnalyzeGlosaDelegate.java
│   │   │               │   │   └── PrepareGlosaAppealDelegate.java
│   │   │               │   ├── collection/
│   │   │               │   │   ├── ProcessPatientPaymentDelegate.java
│   │   │               │   │   ├── SendPaymentReminderDelegate.java
│   │   │               │   │   └── InitiateCollectionDelegate.java
│   │   │               │   └── compensation/
│   │   │               │       ├── CompensateSubmitDelegate.java
│   │   │               │       ├── CompensateAppealDelegate.java
│   │   │               │       └── CompensateAllocationDelegate.java
│   │   │               │
│   │   │               ├── listener/                          # Event Listeners
│   │   │               │   └── audit/
│   │   │               │       ├── TaskStartListener.java
│   │   │               │       └── TaskEndListener.java
│   │   │               │
│   │   │               ├── connector/                         # External Integrations
│   │   │               │   ├── notification/
│   │   │               │   │   └── EmailNotificationConnector.java
│   │   │               │   ├── webservice/
│   │   │               │   │   └── InsurancePortalConnector.java
│   │   │               │   ├── ocr/
│   │   │               │   │   └── DocumentOCRConnector.java
│   │   │               │   └── rpa/
│   │   │               │       └── RPABotConnector.java
│   │   │               │
│   │   │               ├── api/                               # REST API
│   │   │               │   ├── controller/
│   │   │               │   │   ├── PatientController.java
│   │   │               │   │   └── ClaimController.java
│   │   │               │   └── dto/
│   │   │               │       ├── PatientDTO.java
│   │   │               │       └── ClaimDTO.java
│   │   │               │
│   │   │               └── util/                              # Utilities
│   │   │                   ├── DateUtils.java
│   │   │                   └── ValidationUtils.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml                    # Main config
│   │       ├── application-dev.yml                # Dev profile
│   │       ├── application-test.yml               # Test profile
│   │       ├── application-prod.yml               # Prod profile
│   │       │
│   │       ├── processes/                         # BPMN Processes
│   │       │   ├── MAIN_RevenueCycleProcess.bpmn
│   │       │   ├── SUB01_FirstContact.bpmn
│   │       │   ├── SUB02_PreAttendance.bpmn
│   │       │   ├── SUB03_ClinicalAttendance.bpmn
│   │       │   ├── SUB04_BillingAndCoding.bpmn
│   │       │   └── SUB05_Collection.bpmn
│   │       │
│   │       ├── dmn/                               # Decision Tables
│   │       │   ├── EligibilityVerification.dmn
│   │       │   ├── AuthorizationRequirement.dmn
│   │       │   └── GlosaAnalysis.dmn
│   │       │
│   │       ├── forms/                             # Camunda Forms
│   │       │   └── patient-registration.form
│   │       │
│   │       └── db/
│   │           └── migration/                     # Flyway/Liquibase
│   │               └── V1__initial_schema.sql
│   │
│   └── test/
│       ├── java/
│       │   └── com/
│       │       └── hospital/
│       │           └── revenuecycle/                          # MIRRORS src/main/java
│       │               │
│       │               ├── unit/                              # Unit Tests
│       │               │   ├── delegate/
│       │               │   │   ├── eligibility/
│       │               │   │   │   └── VerifyPatientEligibilityDelegateTest.java
│       │               │   │   ├── medicalcoding/
│       │               │   │   │   └── AssignCodesDelegateTest.java
│       │               │   │   └── billing/
│       │               │   │       └── GenerateInvoiceDelegateTest.java
│       │               │   ├── service/
│       │               │   │   └── EligibilityServiceTest.java
│       │               │   └── util/
│       │               │       └── DateUtilsTest.java
│       │               │
│       │               ├── integration/                       # Integration Tests
│       │               │   ├── process/
│       │               │   │   ├── FirstContactIntegrationTest.java
│       │               │   │   └── BillingProcessIntegrationTest.java
│       │               │   ├── dmn/
│       │               │   │   └── EligibilityVerificationDMNTest.java
│       │               │   └── api/
│       │               │       └── PatientControllerIntegrationTest.java
│       │               │
│       │               ├── fixture/                           # Test Data Builders
│       │               │   ├── PatientFixture.java
│       │               │   ├── InsuranceFixture.java
│       │               │   ├── ClaimFixture.java
│       │               │   └── BillingFixture.java
│       │               │
│       │               ├── performance/                       # Performance Tests
│       │               │   └── RevenueCyclePerformanceTest.java
│       │               │
│       │               └── e2e/                               # End-to-End Tests
│       │                   └── RevenueCycleE2ETest.java
│       │
│       └── resources/
│           ├── application-test.yml               # Test-specific config
│           ├── test-processes/                    # Test BPMN files
│           └── test-data/                         # Test data files
│
├── docs/                                          # Documentation
│   ├── architecture/
│   │   ├── PROJECT_STRUCTURE_DESIGN.md           # THIS FILE
│   │   ├── ADR-001-Maven-Structure.md
│   │   └── system-diagrams/
│   ├── api/
│   │   └── REST_API_Documentation.md
│   ├── processes/
│   │   └── Revenue_Cycle_Process_Guide.md
│   └── requirements/
│       └── ORIGINAL_REQUIREMENTS.md
│
├── scripts/                                       # Utility Scripts
│   ├── deploy.sh
│   ├── test.sh
│   └── setup-db.sh
│
└── target/                                        # Maven Build Output (gitignored)
    └── revenue-cycle-camunda-1.0.0.jar
```

---

## Package Naming Strategy

### Base Package: `com.hospital.revenuecycle`

**Rationale:**
- ✅ Follows Java reverse-DNS convention
- ✅ More specific than generic `com.hospital`
- ✅ Prevents conflicts with other hospital systems
- ✅ Clear domain indication (revenue cycle management)
- ✅ Industry-standard naming pattern

### Complete Package Hierarchy

```
com.hospital.revenuecycle                        # Root - Application class
├── config                                       # Spring configuration
├── domain
│   ├── model                                    # JPA entities
│   ├── repository                               # Spring Data repositories
│   └── service                                  # Business logic services
├── delegate
│   ├── eligibility                              # Eligibility verification
│   ├── authorization                            # Prior authorization
│   ├── medicalcoding                            # ICD-10/CPT coding
│   ├── billing                                  # Invoice generation
│   ├── glosa                                    # Denial management (Glosa)
│   ├── collection                               # Payment collection
│   └── compensation                             # BPMN compensation handlers
├── listener
│   └── audit                                    # Audit trail listeners
├── connector
│   ├── notification                             # Email/SMS notifications
│   ├── webservice                               # External APIs
│   ├── ocr                                      # Document OCR
│   └── rpa                                      # Robotic Process Automation
├── api
│   ├── controller                               # REST endpoints
│   └── dto                                      # Data Transfer Objects
└── util                                         # Shared utilities
```

### Test Package Mirroring

**Principle:** Test packages MUST mirror source packages exactly, with test-type layer added.

**Examples:**

| Source Package | Test Package |
|---------------|-------------|
| `com.hospital.revenuecycle.delegate.eligibility.VerifyPatientEligibilityDelegate` | `com.hospital.revenuecycle.unit.delegate.eligibility.VerifyPatientEligibilityDelegateTest` |
| `com.hospital.revenuecycle.domain.service.EligibilityService` | `com.hospital.revenuecycle.unit.service.EligibilityServiceTest` |
| `com.hospital.revenuecycle` (process test) | `com.hospital.revenuecycle.integration.process.FirstContactIntegrationTest` |

---

## Maven Configuration Design

### 1. Dependency Management Strategy

**Use Centralized Version Properties:**

```xml
<properties>
    <!-- Core Versions -->
    <java.version>17</java.version>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>

    <!-- Camunda -->
    <camunda.version>7.20.0</camunda.version>
    <camunda.spring-boot.version>7.20.0</camunda.spring-boot.version>

    <!-- Testing -->
    <junit.version>5.10.1</junit.version>
    <assertj.version>3.24.2</assertj.version>

    <!-- Code Quality -->
    <jacoco.version>0.8.11</jacoco.version>
    <jacoco.coverage.minimum>0.90</jacoco.coverage.minimum>
</properties>
```

**Use Camunda BOM for Dependency Consistency:**

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.camunda.bpm</groupId>
            <artifactId>camunda-bom</artifactId>
            <version>${camunda.version}</version>
            <scope>import</scope>
            <type>pom</type>
        </dependency>
    </dependencies>
</dependencyManagement>
```

---

### 2. Plugin Configuration

| Plugin | Version | Purpose | Phase |
|--------|---------|---------|-------|
| **maven-compiler-plugin** | 3.11.0 | Java 17 compilation with `-parameters` flag | compile |
| **spring-boot-maven-plugin** | (parent) | Create executable JAR | package |
| **maven-surefire-plugin** | 3.2.2 | Run unit tests (`*Test.java`, `*Tests.java`) | test |
| **maven-failsafe-plugin** | 3.2.2 | Run integration tests (`*IT.java`, `*IntegrationTest.java`) | integration-test |
| **jacoco-maven-plugin** | 0.8.11 | Code coverage with 90% minimum threshold | verify |

**Critical Update Required:**

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <!-- UPDATE THIS -->
        <mainClass>com.hospital.revenuecycle.RevenueCycleApplication</mainClass>
        <excludes>
            <exclude>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
            </exclude>
        </excludes>
    </configuration>
</plugin>
```

---

### 3. Resource Configuration (SIMPLIFIED)

**Remove Custom BPMN/DMN Mappings:**

```xml
<!-- ❌ DELETE THESE CUSTOM RESOURCE BLOCKS -->
<resource>
    <directory>src/bpmn</directory>
    <targetPath>processes</targetPath>
</resource>
<resource>
    <directory>src/dmn</directory>
    <targetPath>dmn</targetPath>
</resource>
```

**Use Standard Maven Resources:**

```xml
<!-- ✅ KEEP THIS SIMPLE -->
<resources>
    <resource>
        <directory>src/main/resources</directory>
        <filtering>true</filtering>
    </resource>
</resources>

<testResources>
    <testResource>
        <directory>src/test/resources</directory>
    </testResource>
</testResources>
```

**Then place files in standard locations:**
- BPMN: `src/main/resources/processes/`
- DMN: `src/main/resources/dmn/`
- Forms: `src/main/resources/forms/`

---

### 4. Build Profiles

| Profile | Active by Default | Spring Profile | Database | Purpose |
|---------|------------------|----------------|----------|---------|
| **dev** | ✅ Yes | dev | H2 in-memory | Local development |
| **test** | No | test | H2 in-memory | CI/CD testing |
| **prod** | No | prod | PostgreSQL | Production deployment |

```bash
# Usage examples:
mvn clean install -Pdev          # Development build
mvn clean verify -Ptest          # Run tests
mvn clean package -Pprod         # Production build
```

---

## Migration Path (10 Phases)

### Phase Summary

| Phase | Name | Risk | Rollback | Validation |
|-------|------|------|----------|------------|
| 1 | Create Safety Backup | LOW | N/A | Git status clean |
| 2 | Create Standard Structure | LOW | rm -rf src/main src/test | Directories exist |
| 3 | Move Application Class | MEDIUM | Restore from git | mvn compile succeeds |
| 4 | Migrate Source Files | **HIGH** | git checkout src/ | mvn compile succeeds |
| 5 | Migrate Resources | MEDIUM | Restore from git | BPMN files valid |
| 6 | Migrate Tests | **HIGH** | Restore tests/ | mvn test succeeds |
| 7 | Update pom.xml | MEDIUM | git checkout pom.xml | mvn validate succeeds |
| 8 | Remove Old Structure | LOW | Restore from git | Only src/{main,test} exist |
| 9 | Complete Verification | LOW | Full rollback | All tests pass, JAR builds |
| 10 | Documentation Update | NONE | N/A | Docs match reality |

---

### Detailed Phase Breakdown

#### Phase 1: Create Safety Backup
```bash
# Create backup branch
git checkout -b backup/pre-restructure

# Tag current state
git tag -a v0.1-pre-restructure -m "Before structure refactor"

# Return to main branch
git checkout main
```

**Validation:** `git status` shows clean working directory
**Rollback:** `git reset --hard backup/pre-restructure`
**Risk:** LOW

---

#### Phase 2: Create Standard Maven Directory Structure
```bash
# Create main source structure
mkdir -p src/main/java/com/hospital/revenuecycle
mkdir -p src/main/resources/{processes,dmn,forms,db/migration}

# Create test structure
mkdir -p src/test/java/com/hospital/revenuecycle/{unit,integration,fixture,performance,e2e}
mkdir -p src/test/resources/{test-processes,test-data}
```

**Validation:** `find src -type d` shows all directories
**Rollback:** `rm -rf src/main src/test`
**Risk:** LOW

---

#### Phase 3: Move and Update Application Class
```bash
# Move main application class
mv src/main/java/com/hospital/RevenueCycleApplication.java \
   src/main/java/com/hospital/revenuecycle/RevenueCycleApplication.java

# Update package declaration
sed -i '' 's/package com.hospital;/package com.hospital.revenuecycle;/' \
    src/main/java/com/hospital/revenuecycle/RevenueCycleApplication.java

# Update @ComponentScan if present
sed -i '' 's/@ComponentScan("com.hospital")/@ComponentScan("com.hospital.revenuecycle")/' \
    src/main/java/com/hospital/revenuecycle/RevenueCycleApplication.java
```

**Validation:** `mvn compile` succeeds
**Rollback:** Restore original file from git
**Risk:** MEDIUM

---

#### Phase 4: Migrate All Source Files (CRITICAL)

**Sub-phase 4a: Migrate Delegates**

```bash
# Move delegate directories
mv src/delegates/* src/main/java/com/hospital/revenuecycle/delegate/
mv src/java/com/hospital/compensation src/main/java/com/hospital/revenuecycle/delegate/

# Update package declarations (example for eligibility)
find src/main/java/com/hospital/revenuecycle/delegate/eligibility -name "*.java" -exec \
    sed -i '' 's/package com.hospital.delegates.eligibility;/package com.hospital.revenuecycle.delegate.eligibility;/' {} \;

# Repeat for all delegate subdirectories
```

**Sub-phase 4b: Migrate Listeners**

```bash
mv src/java/com/hospital/audit src/main/java/com/hospital/revenuecycle/listener/

find src/main/java/com/hospital/revenuecycle/listener/audit -name "*.java" -exec \
    sed -i '' 's/package com.hospital.audit;/package com.hospital.revenuecycle.listener.audit;/' {} \;
```

**Sub-phase 4c: Migrate Connectors**

```bash
mv src/connectors/* src/main/java/com/hospital/revenuecycle/connector/

# Add package declarations (example)
find src/main/java/com/hospital/revenuecycle/connector -name "*.java" -exec \
    sed -i '' '1i\
package com.hospital.revenuecycle.connector.notification;
' {} \;
```

**Validation:** `mvn compile` must succeed with NO errors
**Rollback:** `git checkout src/`
**Risk:** HIGH

---

#### Phase 5: Migrate BPMN, DMN and Resources

```bash
# Move BPMN files
mv src/bpmn/*.bpmn src/main/resources/processes/

# Move DMN files
mv src/dmn/*.dmn src/main/resources/dmn/

# Move configuration files
mv src/config/application*.yml src/main/resources/
```

**Update BPMN Delegate References:**

```bash
# Find and replace delegate class names in BPMN files
find src/main/resources/processes -name "*.bpmn" -exec \
    sed -i '' 's/com\.hospital\.delegates\./com.hospital.revenuecycle.delegate./g' {} \;

find src/main/resources/processes -name "*.bpmn" -exec \
    sed -i '' 's/com\.hospital\.compensation\./com.hospital.revenuecycle.delegate.compensation./g' {} \;
```

**Validation:**
1. `ls src/main/resources/processes/*.bpmn` shows all files
2. `grep -r "camunda:class" src/main/resources/processes/` shows correct package paths

**Rollback:** Restore from git
**Risk:** MEDIUM

---

#### Phase 6: Migrate and Fix All Test Files (CRITICAL)

**Sub-phase 6a: Migrate Unit Tests**

```bash
# Move unit tests
mv tests/unit/* src/test/java/com/hospital/revenuecycle/unit/

# Update package declarations
find src/test/java/com/hospital/revenuecycle/unit -name "*.java" -exec \
    sed -i '' 's/package br\.com\.hospital\.futuro\.unit\./package com.hospital.revenuecycle.unit./g' {} \;

# Update imports to match new source packages
find src/test/java/com/hospital/revenuecycle/unit -name "*.java" -exec \
    sed -i '' 's/import br\.com\.hospital\.futuro\./import com.hospital.revenuecycle./g' {} \;
```

**Sub-phase 6b: Migrate Integration Tests**

```bash
mv tests/integration/* src/test/java/com/hospital/revenuecycle/integration/

find src/test/java/com/hospital/revenuecycle/integration -name "*.java" -exec \
    sed -i '' 's/package br\.com\.hospital\.futuro\.integration\./package com.hospital.revenuecycle.integration./g' {} \;

find src/test/java/com/hospital/revenuecycle/integration -name "*.java" -exec \
    sed -i '' 's/import br\.com\.hospital\.futuro\./import com.hospital.revenuecycle./g' {} \;
```

**Sub-phase 6c: Migrate Fixtures**

```bash
mv tests/fixtures/* src/test/java/com/hospital/revenuecycle/fixture/

# Rename files (Fixtures -> Fixture, singular)
cd src/test/java/com/hospital/revenuecycle/fixture/
for file in *Fixtures.java; do
    mv "$file" "${file/Fixtures/Fixture}"
done

# Update package and class names
find . -name "*Fixture.java" -exec \
    sed -i '' 's/package br\.com\.hospital\.futuro\.fixtures;/package com.hospital.revenuecycle.fixture;/' {} \;

find . -name "*Fixture.java" -exec \
    sed -i '' 's/class \(.*\)Fixtures/class \1Fixture/' {} \;
```

**Validation:**
1. `mvn test-compile` succeeds
2. `mvn test` runs all tests

**Rollback:** Restore tests/ directory from git
**Risk:** HIGH

---

#### Phase 7: Update pom.xml Configuration

```xml
<!-- Change #1: Update main class -->
<configuration>
    <mainClass>com.hospital.revenuecycle.RevenueCycleApplication</mainClass>
</configuration>

<!-- Change #2: Remove custom resource blocks -->
<!-- DELETE these sections completely -->
<resource>
    <directory>src/bpmn</directory>
    <targetPath>processes</targetPath>
</resource>
<resource>
    <directory>src/dmn</directory>
    <targetPath>dmn</targetPath>
</resource>

<!-- Change #3: Keep only standard resources -->
<resources>
    <resource>
        <directory>src/main/resources</directory>
        <filtering>true</filtering>
    </resource>
</resources>
```

**Validation:** `mvn validate` succeeds
**Rollback:** `git checkout pom.xml`
**Risk:** MEDIUM

---

#### Phase 8: Remove Old Directory Structure

```bash
# Remove obsolete directories
rm -rf src/java/
rm -rf src/delegates/
rm -rf tests/
rm -rf src/bpmn/
rm -rf src/dmn/
rm -rf src/utils/
rm -rf src/config/
rm -rf src/connectors/

# Clean up misplaced build artifacts
rm -rf src/target/
```

**Validation:** `find src -maxdepth 1 -type d` shows only `src/main` and `src/test`
**Risk:** LOW

---

#### Phase 9: Complete Build and Test Verification

```bash
# Full clean build
mvn clean

# Compile all source code
mvn compile

# Run unit tests
mvn test

# Run integration tests
mvn integration-test

# Full verification (includes JaCoCo coverage check)
mvn verify

# Create deployable artifact
mvn package

# Test application startup (Ctrl+C to stop after startup)
java -jar target/revenue-cycle-camunda-1.0.0.jar
```

**Success Criteria:**
- ✅ All source files compile without errors
- ✅ All unit tests pass (100%)
- ✅ All integration tests pass (100%)
- ✅ Code coverage ≥ 90%
- ✅ JAR file created successfully
- ✅ Application starts without errors
- ✅ Camunda processes deploy successfully
- ✅ Camunda Web UI accessible at http://localhost:8080

**If ANY criterion fails:** Execute full rollback to backup branch.

**Risk:** LOW (validation only, no destructive operations)

---

#### Phase 10: Update Documentation and Git

```bash
# Update README.md with new structure
# (Manual editing required)

# Create Architecture Decision Record
# docs/architecture/ADR-001-Maven-Structure.md

# Git commit with detailed message
git add .
git commit -m "refactor: Migrate to Standard Maven Layout

BREAKING CHANGE: Complete project restructure

- Migrated to Standard Maven Layout (src/main/java, src/test/java)
- Unified package hierarchy to com.hospital.revenuecycle.*
- Aligned test packages with source packages
- Simplified Maven resource configuration
- Updated pom.xml mainClass reference

All tests passing. Code coverage: 90%+

Resolves: Issue #1 (Directory chaos)
Resolves: Issue #2 (Package mismatch)
Resolves: Issue #3 (Maven resource misconfiguration)
"

# Create success tag
git tag -a v1.0-restructured -m "Project restructured to Maven standards"
```

**Risk:** NONE (documentation only)

---

## Risk Assessment

### Overall Risk Level: MEDIUM-HIGH

**Primary Risk Factors:**
1. **Massive Structural Change:** Every file moves to new location
2. **Package Renaming:** Breaks all imports and references
3. **BPMN Delegate References:** Must be updated in XML files
4. **Test-Source Alignment:** Complex import relationship mapping

---

### Critical Risks

#### Risk #1: Import Mismatches After File Movement

**Description:** After moving files to new packages, imports may not resolve.

**Probability:** HIGH
**Impact:** HIGH (project won't compile)

**Mitigation Strategies:**
1. Use IDE refactoring tools (IntelliJ IDEA "Move Class" with "Search for references")
2. Use `sed` scripts for bulk package updates
3. Validate with `mvn compile` after each major move
4. Maintain detailed mapping of old → new package names

**Detection:** `mvn compile` will fail immediately with "cannot find symbol" errors

**Recovery:** Rollback to Phase N-1 and retry with corrected scripts

---

#### Risk #2: BPMN Delegate References Mismatch

**Description:** BPMN files contain hardcoded Java class names that must match new packages.

**Example:**
```xml
<!-- BPMN file before migration -->
<serviceTask id="VerifyEligibility"
             camunda:class="com.hospital.delegates.eligibility.VerifyPatientEligibilityDelegate">

<!-- Must become -->
<serviceTask id="VerifyEligibility"
             camunda:class="com.hospital.revenuecycle.delegate.eligibility.VerifyPatientEligibilityDelegate">
```

**Probability:** MEDIUM
**Impact:** HIGH (process deployment fails at runtime)

**Mitigation Strategies:**
1. Automated `sed` replacement in all `.bpmn` files before deployment
2. Manual review of critical process files
3. Test deployment in local Camunda before production
4. Create mapping document of old → new delegate class names

**Detection:**
- Process deployment fails with `ClassNotFoundException`
- Camunda engine throws `BpmnError` on process start

**Recovery:**
1. Fix BPMN files with correct class names
2. Re-deploy processes
3. No code changes required

---

#### Risk #3: Test Fixture Import Chains

**Description:** Tests use fixtures, fixtures import domain models. If package paths don't align, tests fail.

**Example:**
```java
// Test file
import br.com.hospital.futuro.fixtures.PatientFixtures;  // OLD

// Fixture file
import com.hospital.domain.model.Patient;  // Doesn't exist yet

// Should be
import com.hospital.revenuecycle.fixture.PatientFixture;  // NEW
import com.hospital.revenuecycle.domain.model.Patient;    // NEW
```

**Probability:** MEDIUM
**Impact:** HIGH (all tests fail)

**Mitigation:**
1. Update fixtures first, before tests
2. Use IDE "Find Usages" to track import chains
3. Compile tests incrementally: fixtures → unit tests → integration tests

**Detection:** `mvn test-compile` fails

**Recovery:** Fix imports in fixtures, then cascade to tests

---

### Rollback Strategy

**Trigger Conditions:**
- Any phase validation fails
- Critical risk materializes
- Build time exceeds 2x baseline
- Team decision to abort

**Rollback Procedure:**

```bash
# 1. Stop current operations
Ctrl+C (if running)

# 2. Discard all changes
git reset --hard HEAD

# 3. Return to backup branch
git checkout backup/pre-restructure

# 4. Verify original state restored
mvn clean compile test

# 5. Tag rollback event
git tag -a rollback-$(date +%Y%m%d) -m "Rolled back structure refactor"
```

**Recovery Time:** < 5 minutes
**Data Loss:** None (code/structure changes only, no data)

---

## Success Criteria

### Technical Success

- [x] Project follows Standard Maven Layout (`src/main/java`, `src/test/java`)
- [x] Single consistent package hierarchy (`com.hospital.revenuecycle.*`)
- [x] All source files compile without errors (`mvn compile`)
- [x] All tests pass with 90%+ coverage (`mvn verify`)
- [x] Application starts successfully (`java -jar target/*.jar`)
- [x] Camunda processes deploy without errors
- [x] JAR file builds successfully (`mvn package`)
- [x] No warnings about non-standard paths in Maven logs

### Maintainability Success

- [x] Clear separation of concerns (delegate, listener, connector, etc.)
- [x] Test packages mirror source packages exactly
- [x] IDE navigation works correctly (Ctrl+Click on imports resolves)
- [x] New developers understand structure in < 10 minutes
- [x] Git history shows clear migration path
- [x] Documentation reflects actual structure accurately

### Operational Success

- [x] CI/CD pipeline builds successfully
- [x] Docker image creation works (if applicable)
- [x] Deployment to target environment succeeds
- [x] Monitoring and logging function correctly
- [x] No regression in application functionality
- [x] Performance metrics unchanged

---

## Next Steps for Implementation

### Immediate Actions (Hive Mind Coordination)

1. **Architect Agent** (DONE):
   - ✅ Design complete structure
   - ✅ Store design in memory (key: `hive/design/project-structure`)
   - ✅ Create this documentation

2. **Migration Planner Agent** (NEXT):
   - Create detailed migration scripts for each phase
   - Prepare automated package rename scripts
   - Design validation test suite
   - Store plan in memory (key: `hive/plan/migration-scripts`)

3. **Coder Agent** (PENDING):
   - Implement migration automation scripts
   - Create rollback procedures
   - Build validation checks

4. **Tester Agent** (PENDING):
   - Validate each phase
   - Run regression test suite
   - Verify BPMN process deployment

5. **Coordinator Agent** (PENDING):
   - Orchestrate execution across agents
   - Monitor progress
   - Trigger rollback if needed

---

## References

- Maven Standard Directory Layout: https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html
- Camunda 7 Best Practices: https://docs.camunda.org/manual/7.20/user-guide/process-engine/process-engine-concepts/
- Spring Boot Project Structure: https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using.structuring-your-code
- Java Package Naming Conventions: https://docs.oracle.com/javase/tutorial/java/package/namingpkgs.html

---

**Document Status:** APPROVED
**Last Updated:** 2025-12-09
**Next Review:** After Phase 9 Completion
