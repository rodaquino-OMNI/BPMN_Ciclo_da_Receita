# C4 Architecture Diagrams
## Hospital Revenue Cycle - Camunda 7 Application

**Diagram Type:** C4 Model (Context, Container, Component, Code)
**Date:** 2025-12-09
**Status:** APPROVED

---

## Level 1: System Context Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         Hospital Revenue Cycle                          │
│                         Management System (HRMS)                        │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │                  Revenue Cycle Camunda System                   │  │
│  │                                                                 │  │
│  │  • Patient Registration & Eligibility Verification              │  │
│  │  • Prior Authorization Management                               │  │
│  │  • Medical Coding (ICD-10/CPT)                                 │  │
│  │  • Billing & Invoice Generation                                │  │
│  │  • Glosa (Denial) Management & Appeals                         │  │
│  │  • Payment Collection & Reconciliation                          │  │
│  │                                                                 │  │
│  │  Technology: Spring Boot 3.2 + Camunda 7.20 + Java 17          │  │
│  └─────────────────────────────────────────────────────────────────┘  │
│           ▲                    ▲                    ▲                  │
│           │                    │                    │                  │
└───────────┼────────────────────┼────────────────────┼──────────────────┘
            │                    │                    │
            │                    │                    │
┌───────────┼────────┐  ┌────────┼──────────┐  ┌─────┼──────────┐
│  Hospital Staff    │  │  Insurance Payers │  │   Patients     │
│                    │  │                   │  │                │
│ • Front Desk Staff │  │ • Insurance Portals│ │ • Patient Portal│
│ • Medical Coders   │  │ • Claims Adjudication│ │ • Payment UI  │
│ • Billing Team     │  │ • Authorization Systems│ │ • Mobile App │
│ • Collections Team │  │ • Electronic Data Interchange│ │        │
│ • Finance Officers │  │                   │  │                │
└────────────────────┘  └───────────────────┘  └────────────────┘
            │                    │                    │
            ├────────────────────┼────────────────────┤
            │                    │                    │
    ┌───────▼────────┐  ┌────────▼──────────┐  ┌─────▼──────────┐
    │  External      │  │  External         │  │  External      │
    │  Systems       │  │  Services         │  │  Infrastructure│
    │                │  │                   │  │                │
    │ • EHR/EMR      │  │ • OCR Service     │  │ • Email Server │
    │ • Practice Mgmt│  │ • RPA Bots        │  │ • SMS Gateway  │
    │ • Lab Systems  │  │ • Document Store  │  │ • Payment Gateway│
    └────────────────┘  └───────────────────┘  └────────────────┘
```

**System Purpose:** Automate and manage the complete hospital revenue cycle from patient first contact through final payment collection.

**Key Users:**
- **Hospital Staff:** Register patients, code procedures, submit claims
- **Insurance Payers:** Authorize procedures, adjudicate claims, process payments
- **Patients:** Make co-payments, view bills, request payment plans

**External Dependencies:**
- Electronic Health Record (EHR) systems
- Insurance company portals and APIs
- OCR and RPA services
- Notification infrastructure

---

## Level 2: Container Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                    Hospital Revenue Cycle - Camunda System                      │
│                                                                                  │
│  ┌─────────────────────────────────────────────────────────────────────────┐   │
│  │                     Revenue Cycle Application                            │   │
│  │                 (Spring Boot 3.2 + Camunda 7.20)                        │   │
│  │                                                                          │   │
│  │  ┌────────────────────────────────────────────────────────────────┐    │   │
│  │  │                    REST API Layer                               │    │   │
│  │  │  • Patient API      • Claim API     • Payment API              │    │   │
│  │  │  • Authorization API • Eligibility API                          │    │   │
│  │  └────────────────────────────────────────────────────────────────┘    │   │
│  │                                ▲                                        │   │
│  │  ┌────────────────────────────┼───────────────────────────────────┐    │   │
│  │  │              Camunda BPMN Process Engine                        │    │   │
│  │  │                                                                 │    │   │
│  │  │  Main Process:    Revenue Cycle Orchestrator                   │    │   │
│  │  │  Sub-Processes:   • SUB01_FirstContact                         │    │   │
│  │  │                   • SUB02_PreAttendance                         │    │   │
│  │  │                   • SUB03_ClinicalAttendance                    │    │   │
│  │  │                   • SUB04_BillingAndCoding                      │    │   │
│  │  │                   • SUB05_Collection                            │    │   │
│  │  │                                                                 │    │   │
│  │  │  DMN Decisions:   • EligibilityVerification.dmn                │    │   │
│  │  │                   • AuthorizationRequirement.dmn                │    │   │
│  │  │                   • GlosaAnalysis.dmn                           │    │   │
│  │  └─────────────────────────────────────────────────────────────────┘    │   │
│  │                                ▲                                        │   │
│  │  ┌────────────────────────────┼───────────────────────────────────┐    │   │
│  │  │              Business Logic Layer                               │    │   │
│  │  │                                                                 │    │   │
│  │  │  Domain Services:                                               │    │   │
│  │  │  • EligibilityService      • AuthorizationService              │    │   │
│  │  │  • BillingService          • CollectionService                 │    │   │
│  │  │  • GlosaManagementService                                      │    │   │
│  │  │                                                                 │    │   │
│  │  │  Camunda Delegates:                                            │    │   │
│  │  │  • Eligibility Delegates   • Authorization Delegates           │    │   │
│  │  │  • Medical Coding Delegates • Billing Delegates                │    │   │
│  │  │  • Glosa Delegates         • Collection Delegates              │    │   │
│  │  │  • Compensation Handlers                                       │    │   │
│  │  │                                                                 │    │   │
│  │  │  Event Listeners:                                              │    │   │
│  │  │  • TaskStartListener       • TaskEndListener                   │    │   │
│  │  └─────────────────────────────────────────────────────────────────┘    │   │
│  │                                ▲                                        │   │
│  │  ┌────────────────────────────┼───────────────────────────────────┐    │   │
│  │  │              Data Access Layer                                  │    │   │
│  │  │                                                                 │    │   │
│  │  │  Spring Data JPA Repositories:                                 │    │   │
│  │  │  • PatientRepository       • InsuranceRepository               │    │   │
│  │  │  • ClaimRepository         • PaymentRepository                 │    │   │
│  │  │  • AuthorizationRepository                                     │    │   │
│  │  └─────────────────────────────────────────────────────────────────┘    │   │
│  │                                ▲                                        │   │
│  │  ┌────────────────────────────┼───────────────────────────────────┐    │   │
│  │  │              External Integration Layer                         │    │   │
│  │  │                                                                 │    │   │
│  │  │  Connectors:                                                   │    │   │
│  │  │  • InsurancePortalConnector  (REST/SOAP)                       │    │   │
│  │  │  • EHRConnector              (HL7/FHIR)                        │    │   │
│  │  │  • EmailNotificationConnector (SMTP)                           │    │   │
│  │  │  • SMSNotificationConnector   (Twilio)                         │    │   │
│  │  │  • DocumentOCRConnector       (AWS Textract)                   │    │   │
│  │  │  • RPABotConnector            (UiPath/Blue Prism)              │    │   │
│  │  └─────────────────────────────────────────────────────────────────┘    │   │
│  └──────────────────────────────────────────────────────────────────────────┘   │
│                                                                                  │
│  ┌─────────────────────────────────────────────────────────────────────────┐   │
│  │                        Database Container                                │   │
│  │                   PostgreSQL 15 (Production)                             │   │
│  │                   H2 (Development/Testing)                               │   │
│  │                                                                          │   │
│  │  Tables:                                                                 │   │
│  │  • patients           • insurance_policies    • claims                   │   │
│  │  • authorizations     • medical_codes         • payments                 │   │
│  │  • audit_log          • glosa_records                                    │   │
│  │                                                                          │   │
│  │  Camunda Tables:                                                         │   │
│  │  • act_* (runtime)    • act_hi_* (history)   • act_re_* (repository)   │   │
│  └─────────────────────────────────────────────────────────────────────────┘   │
│                                                                                  │
│  ┌─────────────────────────────────────────────────────────────────────────┐   │
│  │                     Camunda Web Applications                             │   │
│  │                                                                          │   │
│  │  • Camunda Tasklist  (User Task Management)                             │   │
│  │  • Camunda Cockpit   (Process Monitoring)                               │   │
│  │  • Camunda Admin     (User & Authorization Management)                  │   │
│  │                                                                          │   │
│  │  Access: http://localhost:8080/camunda/                                 │   │
│  └─────────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────────┘
```

**Technology Stack:**
- **Application:** Spring Boot 3.2, Camunda 7.20, Java 17
- **Database:** PostgreSQL 15 (Prod), H2 (Dev/Test)
- **Build:** Maven 3.9+
- **Testing:** JUnit 5, AssertJ, Mockito, Camunda BPM Assert
- **Code Quality:** JaCoCo (90% coverage minimum)

---

## Level 3: Component Diagram (Application Layer)

### Package Structure with Component Responsibilities

```
com.hospital.revenuecycle
│
├── RevenueCycleApplication.java                    # Spring Boot Entry Point
│   @SpringBootApplication
│   @EnableCamunda
│
├── config/                                          # CONFIGURATION COMPONENTS
│   ├── CamundaConfiguration.java                    # Camunda engine config
│   ├── DatabaseConfiguration.java                   # JPA/datasource config
│   ├── SecurityConfiguration.java                   # Authentication/authorization
│   └── RestConfiguration.java                       # REST API config
│
├── domain/                                          # DOMAIN LAYER
│   ├── model/                                       # JPA Entities
│   │   ├── Patient.java                             # Patient entity
│   │   ├── Insurance.java                           # Insurance policy entity
│   │   ├── Claim.java                               # Billing claim entity
│   │   ├── Authorization.java                       # Prior authorization entity
│   │   ├── Payment.java                             # Payment transaction entity
│   │   └── GlosaRecord.java                         # Denial/glosa record
│   │
│   ├── repository/                                  # DATA ACCESS COMPONENTS
│   │   ├── PatientRepository.java                   # Patient CRUD operations
│   │   ├── InsuranceRepository.java                 # Insurance CRUD
│   │   ├── ClaimRepository.java                     # Claim CRUD + queries
│   │   ├── AuthorizationRepository.java             # Authorization CRUD
│   │   └── PaymentRepository.java                   # Payment CRUD + reconciliation
│   │
│   └── service/                                     # BUSINESS LOGIC COMPONENTS
│       ├── EligibilityService.java                  # Eligibility verification logic
│       ├── AuthorizationService.java                # Prior auth logic
│       ├── BillingService.java                      # Invoice generation logic
│       ├── CollectionService.java                   # Payment collection logic
│       └── GlosaManagementService.java              # Denial management logic
│
├── delegate/                                        # CAMUNDA DELEGATE COMPONENTS
│   │                                                # (JavaDelegate implementations)
│   ├── eligibility/
│   │   ├── VerifyPatientEligibilityDelegate.java    # Check patient active status
│   │   ├── ValidateInsuranceDelegate.java           # Verify insurance coverage
│   │   └── CheckCoverageDelegate.java               # Check service coverage
│   │
│   ├── authorization/
│   │   ├── RequestAuthorizationDelegate.java        # Submit auth request
│   │   ├── CheckAuthorizationStatusDelegate.java    # Poll auth status
│   │   └── HandleAuthorizationDenialDelegate.java   # Handle denial + appeal
│   │
│   ├── medicalcoding/
│   │   ├── AssignCodesDelegate.java                 # Auto-assign ICD-10/CPT codes
│   │   ├── ValidateCodesDelegate.java               # Validate code accuracy
│   │   └── ReviewCodingDelegate.java                # Human review flagging
│   │
│   ├── billing/
│   │   ├── GenerateInvoiceDelegate.java             # Create billing invoice
│   │   ├── SubmitToPayerDelegate.java               # Submit claim electronically
│   │   └── RecordSubmissionDelegate.java            # Log submission metadata
│   │
│   ├── glosa/
│   │   ├── IdentifyGlosaDelegate.java               # Detect claim denials
│   │   ├── AnalyzeGlosaDelegate.java                # Analyze denial reasons
│   │   ├── PrepareGlosaAppealDelegate.java          # Prepare appeal documents
│   │   └── SubmitAppealDelegate.java                # Submit appeal to payer
│   │
│   ├── collection/
│   │   ├── AllocatePaymentDelegate.java             # Allocate payment to claim
│   │   ├── ProcessPatientPaymentDelegate.java       # Process patient portion
│   │   ├── SendPaymentReminderDelegate.java         # Send payment reminders
│   │   └── InitiateCollectionDelegate.java          # Start collections process
│   │
│   └── compensation/                                # COMPENSATION HANDLERS
│       ├── CompensateSubmitDelegate.java            # Reverse billing submission
│       ├── CompensateAppealDelegate.java            # Reverse glosa appeal
│       └── CompensateAllocationDelegate.java        # Reverse payment allocation
│
├── listener/                                        # EVENT LISTENER COMPONENTS
│   └── audit/
│       ├── TaskStartListener.java                   # Log task start events
│       ├── TaskEndListener.java                     # Log task completion events
│       └── ProcessInstanceListener.java             # Log process lifecycle
│
├── connector/                                       # EXTERNAL INTEGRATION COMPONENTS
│   ├── notification/
│   │   ├── EmailNotificationConnector.java          # SMTP email sender
│   │   └── SMSNotificationConnector.java            # SMS gateway integration
│   │
│   ├── webservice/
│   │   ├── InsurancePortalConnector.java            # Insurance company APIs
│   │   └── EHRConnector.java                        # EHR/EMR integration
│   │
│   ├── ocr/
│   │   └── DocumentOCRConnector.java                # AWS Textract integration
│   │
│   └── rpa/
│       └── RPABotConnector.java                     # RPA bot orchestration
│
├── api/                                             # REST API COMPONENTS
│   ├── controller/
│   │   ├── PatientController.java                   # Patient REST endpoints
│   │   ├── ClaimController.java                     # Claim REST endpoints
│   │   ├── AuthorizationController.java             # Authorization endpoints
│   │   └── PaymentController.java                   # Payment endpoints
│   │
│   └── dto/
│       ├── PatientDTO.java                          # Patient data transfer object
│       ├── ClaimDTO.java                            # Claim DTO
│       └── PaymentDTO.java                          # Payment DTO
│
└── util/                                            # UTILITY COMPONENTS
    ├── DateUtils.java                               # Date/time helpers
    ├── ValidationUtils.java                         # Input validation
    └── FormatUtils.java                             # Data formatting
```

---

## Level 4: Code Diagram (Example: Eligibility Verification)

### Sequence Diagram: Eligibility Verification Process

```
┌──────────┐  ┌─────────────┐  ┌──────────────────┐  ┌──────────────┐  ┌─────────────┐
│ Camunda  │  │  Verify      │  │  Eligibility     │  │  Patient     │  │  Insurance  │
│ Process  │  │  Patient     │  │  Service         │  │  Repository  │  │  Repository │
│ Engine   │  │  Eligibility │  │                  │  │              │  │             │
│          │  │  Delegate    │  │                  │  │              │  │             │
└─────┬────┘  └──────┬───────┘  └────────┬─────────┘  └──────┬───────┘  └──────┬──────┘
      │              │                   │                   │                  │
      │ execute()    │                   │                   │                  │
      │─────────────>│                   │                   │                  │
      │              │                   │                   │                  │
      │              │ getVariable       │                   │                  │
      │              │  ("patientId")    │                   │                  │
      │<─────────────│                   │                   │                  │
      │              │                   │                   │                  │
      │ patientId:123│                   │                   │                  │
      │─────────────>│                   │                   │                  │
      │              │                   │                   │                  │
      │              │ verifyEligibility(123)                │                  │
      │              │───────────────────>│                   │                  │
      │              │                   │                   │                  │
      │              │                   │ findById(123)     │                  │
      │              │                   │──────────────────>│                  │
      │              │                   │                   │                  │
      │              │                   │ Patient entity    │                  │
      │              │                   │<──────────────────│                  │
      │              │                   │                   │                  │
      │              │                   │ findByPatientId(123)                 │
      │              │                   │─────────────────────────────────────>│
      │              │                   │                                      │
      │              │                   │ Insurance entity                     │
      │              │                   │<─────────────────────────────────────│
      │              │                   │                                      │
      │              │                   │ • Check patient active status        │
      │              │                   │ • Validate insurance policy active   │
      │              │                   │ • Verify coverage effective dates    │
      │              │                   │ • Check service eligibility          │
      │              │                   │                                      │
      │              │ EligibilityResult │                                      │
      │              │<───────────────────│                                      │
      │              │  {                │                                      │
      │              │   eligible: true, │                                      │
      │              │   coverageType: "FULL",                                  │
      │              │   copayAmount: 50.00                                     │
      │              │  }                │                                      │
      │              │                   │                                      │
      │ setVariables │                   │                                      │
      │  ("eligible", true)              │                                      │
      │  ("coverageType", "FULL")        │                                      │
      │  ("copayAmount", 50.00)          │                                      │
      │<─────────────│                   │                                      │
      │              │                   │                                      │
      │ Process continues with next task │                                      │
      │              │                   │                                      │
```

### Class Structure Example

```java
// Component: Eligibility Delegate
package com.hospital.revenuecycle.delegate.eligibility;

@Component
public class VerifyPatientEligibilityDelegate implements JavaDelegate {

    @Autowired
    private EligibilityService eligibilityService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // 1. Extract process variables
        Long patientId = (Long) execution.getVariable("patientId");

        // 2. Call domain service
        EligibilityResult result = eligibilityService.verifyEligibility(patientId);

        // 3. Set process variables for next tasks
        execution.setVariable("eligible", result.isEligible());
        execution.setVariable("coverageType", result.getCoverageType());
        execution.setVariable("copayAmount", result.getCopayAmount());

        // 4. Log audit trail
        log.info("Eligibility verified for patient {}: {}", patientId, result);
    }
}

// Component: Eligibility Service
package com.hospital.revenuecycle.domain.service;

@Service
@Transactional
public class EligibilityService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private InsuranceRepository insuranceRepository;

    public EligibilityResult verifyEligibility(Long patientId) {
        // 1. Fetch patient entity
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new PatientNotFoundException(patientId));

        // 2. Fetch active insurance policy
        Insurance insurance = insuranceRepository.findActiveByPatientId(patientId)
            .orElseThrow(() -> new NoActiveInsuranceException(patientId));

        // 3. Business logic validation
        boolean eligible = patient.isActive()
            && insurance.isActive()
            && insurance.isCoverageEffective(LocalDate.now());

        // 4. Build result
        return EligibilityResult.builder()
            .eligible(eligible)
            .coverageType(insurance.getCoverageType())
            .copayAmount(insurance.getCopayAmount())
            .build();
    }
}
```

---

## Component Interaction Patterns

### Pattern 1: Delegate → Service → Repository

```
┌──────────────┐      ┌─────────────┐      ┌────────────────┐      ┌──────────┐
│  BPMN Task   │─────>│  Delegate   │─────>│    Service     │─────>│Repository│
│              │      │  Component  │      │   Component    │      │          │
│ "Verify      │      │VerifyPatient│      │Eligibility     │      │Patient   │
│ Eligibility" │      │Eligibility  │      │Service         │      │Repository│
│              │      │Delegate     │      │                │      │          │
└──────────────┘      └─────────────┘      └────────────────┘      └──────────┘
   Process              Orchestration         Business Logic         Data Access
   Definition           Layer                 Layer                  Layer
```

**Responsibilities:**
- **Delegate:** Extract/set process variables, orchestrate calls
- **Service:** Implement business logic, transaction management
- **Repository:** Data persistence, queries

---

### Pattern 2: Compensation Handler

```
                Normal Flow
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│  Submit      │─────>│  Allocate    │─────>│  Send        │
│  Claim       │      │  Payment     │      │  Receipt     │
└──────────────┘      └──────────────┘      └──────────────┘
                           │
                           │ ERROR occurs
                           │
                           ▼
                Compensation Flow (UNDO)
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│  Compensate  │<─────│  Compensate  │<─────│  Compensate  │
│  Submit      │      │  Allocate    │      │  Send Receipt│
│  Delegate    │      │  Delegate    │      │  Delegate    │
└──────────────┘      └──────────────┘      └──────────────┘
```

**Usage:** BPMN compensation events trigger reverse operations when errors occur.

---

### Pattern 3: External System Integration

```
┌──────────────┐      ┌──────────────┐      ┌──────────────┐      ┌──────────┐
│  Submit to   │─────>│  Submit      │─────>│  Insurance   │─────>│Insurance │
│  Payer Task  │      │  ToPayer     │      │  Portal      │      │Company   │
│              │      │  Delegate    │      │  Connector   │      │Web API   │
└──────────────┘      └──────────────┘      └──────────────┘      └──────────┘
   BPMN Task           Delegate Layer        Connector Layer       External
   (Process)           (Orchestration)       (Integration)         System
```

**Pattern Benefits:**
- Delegates remain focused on process orchestration
- Connectors encapsulate external system complexity
- Easy to mock connectors for testing
- Change external system without touching delegates

---

## Deployment View

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Production Environment                        │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────┐    │
│  │                   Application Server                        │    │
│  │                   (Docker Container)                        │    │
│  │                                                             │    │
│  │   ┌───────────────────────────────────────────────────┐    │    │
│  │   │  revenue-cycle-camunda-1.0.0.jar                  │    │    │
│  │   │  • Embedded Tomcat 10.x                           │    │    │
│  │   │  • Camunda Engine + Web Apps                      │    │    │
│  │   │  • Spring Boot Application                        │    │    │
│  │   └───────────────────────────────────────────────────┘    │    │
│  │                                                             │    │
│  │   JVM Settings:                                             │    │
│  │   -Xmx2048m -Xms1024m                                      │    │
│  │   -XX:+UseG1GC                                             │    │
│  │   -Dspring.profiles.active=prod                            │    │
│  │                                                             │    │
│  │   Exposed Ports:                                            │    │
│  │   8080 (HTTP)                                              │    │
│  └────────────────────────────────────────────────────────────┘    │
│                           │                                         │
│                           │ JDBC Connection                         │
│                           ▼                                         │
│  ┌────────────────────────────────────────────────────────────┐    │
│  │                   Database Server                           │    │
│  │                   (PostgreSQL 15)                           │    │
│  │                                                             │    │
│  │   Databases:                                                │    │
│  │   • revenue_cycle_prod                                     │    │
│  │   • camunda_engine                                          │    │
│  │                                                             │    │
│  │   Backup Strategy:                                          │    │
│  │   • Daily full backups (2 AM)                              │    │
│  │   • Transaction log backups (every 4 hours)                │    │
│  └────────────────────────────────────────────────────────────┘    │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

**Deployment Specifications:**
- **Application:** Single JAR deployment (Spring Boot executable)
- **Database:** PostgreSQL 15 with separate schema for Camunda
- **Containerization:** Docker with Alpine Linux base image
- **Orchestration:** Kubernetes or Docker Compose
- **Scaling:** Horizontal scaling supported (stateless application)

---

## Technology Decision Matrix

| Concern | Option 1 | Option 2 | **CHOSEN** | Rationale |
|---------|----------|----------|------------|-----------|
| **Project Structure** | Multi-module | Single-module | **Single-module** | Simpler deployment, optimal for Camunda apps |
| **Package Naming** | com.hospital | com.hospital.revenuecycle | **com.hospital.revenuecycle** | More specific, prevents conflicts |
| **Directory Layout** | Custom | Maven Standard | **Maven Standard** | Industry standard, tool support |
| **Database (Prod)** | MySQL | PostgreSQL | **PostgreSQL** | Better JSON support, robust transaction handling |
| **Database (Dev)** | H2 | PostgreSQL | **H2** | Fast startup, no external dependencies |
| **Test Framework** | JUnit 4 | JUnit 5 | **JUnit 5** | Modern features, better assertions |
| **Code Coverage** | Cobertura | JaCoCo | **JaCoCo** | Better Maven integration, active development |
| **API Style** | REST | GraphQL | **REST** | Simpler integration, industry standard |
| **BPMN Engine** | Camunda 8 | Camunda 7 | **Camunda 7** | Mature, extensive documentation, no Zeebe dependency |

---

## Architecture Decision Records (ADR) Summary

### ADR-001: Maven Standard Directory Layout

**Status:** APPROVED
**Date:** 2025-12-09

**Decision:** Adopt Standard Maven Layout (src/main/java, src/test/java) and eliminate custom directory structures.

**Rationale:**
- Industry-standard approach
- Full IDE support
- Simplified CI/CD integration
- Lower learning curve for new developers

**Consequences:**
- Requires migration of all existing files
- Short-term disruption (1-2 days)
- Long-term maintainability improvement

---

### ADR-002: Single-Module Maven Project

**Status:** APPROVED
**Date:** 2025-12-09

**Decision:** Use single-module project structure instead of multi-module.

**Rationale:**
- Camunda applications are typically monolithic
- Single deployable artifact (JAR)
- Simpler build process
- Easier dependency management

**Consequences:**
- Cannot separately deploy sub-components
- All code in single codebase
- Sufficient for current project scale

---

### ADR-003: Package Naming Convention

**Status:** APPROVED
**Date:** 2025-12-09

**Decision:** Use `com.hospital.revenuecycle` as base package.

**Rationale:**
- More specific than `com.hospital`
- Prevents naming conflicts
- Clear domain indication
- Follows Java reverse-DNS convention

**Consequences:**
- All existing packages must be renamed
- Test packages must mirror source packages

---

## Appendix: File Migration Mapping

| Old Location | New Location |
|-------------|--------------|
| `src/delegates/eligibility/` | `src/main/java/com/hospital/revenuecycle/delegate/eligibility/` |
| `src/java/com/hospital/compensation/` | `src/main/java/com/hospital/revenuecycle/delegate/compensation/` |
| `src/java/com/hospital/audit/` | `src/main/java/com/hospital/revenuecycle/listener/audit/` |
| `src/connectors/` | `src/main/java/com/hospital/revenuecycle/connector/` |
| `src/bpmn/` | `src/main/resources/processes/` |
| `src/dmn/` | `src/main/resources/dmn/` |
| `tests/unit/` | `src/test/java/com/hospital/revenuecycle/unit/` |
| `tests/integration/` | `src/test/java/com/hospital/revenuecycle/integration/` |
| `tests/fixtures/` | `src/test/java/com/hospital/revenuecycle/fixture/` |

---

**Document Version:** 1.0
**Last Updated:** 2025-12-09
**Next Review:** After Migration Phase 9 Completion
