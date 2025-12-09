# System Architecture Overview - Hospital Revenue Cycle BPMN

## Executive Summary

This document defines the complete system architecture for the Hospital Revenue Cycle automation platform, consisting of 1 orchestrator process and 10 subprocess implementations, fully compatible with Camunda Platform 7.

**Architecture Version**: 1.0.0
**Target Platform**: Camunda Platform 7 (7.18+)
**BPMN Version**: 2.0
**Last Updated**: 2025-12-08

---

## 1. SYSTEM CONTEXT

### 1.1 Business Domain

The Revenue Cycle Management system automates the complete healthcare revenue lifecycle from patient contact through financial reconciliation, integrating with:

- **TASY ERP**: Core hospital information system
- **Insurance Portals**: Health insurance company systems
- **Government Systems**: ANS (regulatory), Receita Federal (tax)
- **Banking Systems**: Payment processing and reconciliation
- **Clinical Systems**: LIS (laboratory), PACS (imaging)
- **IoT Devices**: RFID readers, weight sensors, biometric systems

### 1.2 Strategic Goals

1. **Automation**: Achieve 75%+ automation through RPA, AI, and IoT
2. **Revenue Optimization**: Reduce denials by 40%, accelerate collection by 35%
3. **Compliance**: 100% regulatory compliance (TISS, ANS, LGPD)
4. **Visibility**: Real-time KPIs and predictive analytics
5. **Scalability**: Support 10,000+ patient encounters/month

---

## 2. ARCHITECTURE PATTERNS

### 2.1 Process Orchestration Pattern

```
┌─────────────────────────────────────────────────────────────┐
│                  ORCHESTRATOR PROCESS                        │
│         (ORCH_Ciclo_Receita_Hospital_Futuro)                │
│                                                              │
│  ┌──────┐   ┌──────┐   ┌──────┐   ┌──────┐   ┌──────┐    │
│  │Lane 1│   │Lane 2│   │Lane 3│   │Lane 4│   │Lane 5│    │
│  │SUB_01│ → │SUB_02│ → │SUB_03│ → │SUB_04│ → │SUB_05│    │
│  └──────┘   └──────┘   └──────┘   └──────┘   └──────┘    │
│       ↓          ↓          ↓          ↓          ↓         │
│  ┌──────┐   ┌──────┐   ┌──────┐   ┌──────┐   ┌──────┐    │
│  │Lane 6│   │Lane 7│   │Lane 8│   │Lane 9│   │Lane10│    │
│  │SUB_06│ → │SUB_07│ → │SUB_08│ ⇉ │SUB_09│ ⇉ │SUB_10│    │
│  └──────┘   └──────┘   └──────┘   └──────┘   └──────┘    │
│                                                              │
└─────────────────────────────────────────────────────────────┘
         │              │              │              │
         ↓              ↓              ↓              ↓
    [Patient]    [Insurance]      [TASY]        [Bank]
```

**Pattern**: Hierarchical Orchestration with Call Activities
**Benefits**:
- Independent subprocess deployment
- Clear separation of concerns
- Reusable subprocess components
- Simplified testing and versioning

### 2.2 Communication Patterns

#### Internal Communication (Process-to-Process)
- **Mechanism**: Camunda variables and business keys
- **Scope**: All variables passed via `<camunda:in variables="all">`
- **Business Key**: Propagated to maintain context across subprocesses

#### External Communication (Process-to-System)
- **Mechanism**: Message flows (BPMN 2.0 collaboration)
- **Direction**: Bidirectional between Hospital pool and external participants
- **Implementation**: Service tasks with Camunda connectors

### 2.3 Error Handling Strategy

```yaml
error_handling:
  boundary_events:
    timer:
      - location: SUB_02_Pre_Authorization (48h timeout)
      - location: SUB_07_Denials_Management (ANS deadline)
      - action: Escalation workflow

    error:
      - location: SUB_06_Billing_Submission (transmission failure)
      - action: Retry with exponential backoff (3 attempts)

    signal:
      - location: SUB_04_Clinical_Production (patient discharge)
      - action: Trigger downstream processes

    escalation:
      - location: SUB_07_Denials_Management (critical deadline)
      - action: Management notification

  retry_strategy:
    service_tasks:
      async_before: true
      retry_time_cycle: "R3/PT10M" # 3 retries, 10min intervals
      job_priority: 50 # Default priority
```

---

## 3. PROCESS HIERARCHY

### 3.1 Process Structure

```
Process_ORCH_Revenue_Cycle (Orchestrator)
├── Lane_01: SUB_01_First_Contact (Call Activity)
│   └── Process_SUB_01_First_Contact
│       ├── Lane_Digital_Channels
│       ├── Lane_Call_Center
│       └── Lane_TASY_Scheduling
│
├── Lane_02: SUB_02_Pre_Authorization (Call Activity)
│   └── Process_SUB_02_Pre_Authorization
│       ├── Lane_Eligibility
│       ├── Lane_Authorization
│       ├── Lane_RPA_Portals
│       └── Lane_Appeals
│
├── Lane_03: SUB_03_Admission (Call Activity)
│   └── Process_SUB_03_Admission
│       ├── Lane_Self_Service
│       ├── Lane_Reception
│       ├── Lane_TASY_ADT
│       └── SubProcess_Emergency (Event Subprocess)
│
├── Lane_04: SUB_04_Clinical_Production (Call Activity)
│   └── Process_SUB_04_Clinical_Production
│       ├── Lane_Medical_Team
│       ├── Lane_Nursing
│       ├── Lane_Pharmacy
│       ├── Lane_IoT_RFID
│       ├── Lane_Integration
│       └── SubProcess_Concurrent_Audit (Event Subprocess)
│
├── Lane_05: SUB_05_Coding_Audit (Call Activity)
│   └── Process_SUB_05_Coding_Audit
│       ├── Lane_AI_Coding
│       ├── Lane_Human_Coding
│       ├── Lane_Audit
│       └── Lane_Quality
│
├── Lane_06: SUB_06_Billing_Submission (Call Activity)
│   └── Process_SUB_06_Billing_Submission
│       ├── Lane_Billing
│       ├── Lane_TISS_Engine
│       ├── Lane_Transmission
│       └── Lane_Monitoring
│
├── Lane_07: SUB_07_Denials_Management (Call Activity)
│   └── Process_SUB_07_Denials_Management
│       ├── Lane_Capture
│       ├── Lane_Analysis
│       ├── Lane_LLM_Appeals
│       └── Lane_Negotiation
│
├── Lane_08: SUB_08_Revenue_Collection (Call Activity)
│   └── Process_SUB_08_Revenue_Collection
│       ├── Lane_Bank_Integration
│       ├── Lane_Reconciliation
│       ├── Lane_AR_Management
│       └── Lane_Collection
│
├── Lane_09: SUB_09_Analytics (Call Activity - Async)
│   └── Process_SUB_09_Analytics
│       ├── Lane_Data_Collection
│       ├── Lane_Processing
│       ├── Lane_KPI_Engine
│       └── Lane_Reporting
│
└── Lane_10: SUB_10_Maximization (Call Activity - Async)
    └── Process_SUB_10_Maximization
        ├── Lane_Opportunity_Analysis
        ├── Lane_VBHC
        ├── Lane_Process_Mining
        └── Lane_Continuous_Improvement
```

### 3.2 Process Relationships

| Orchestrator Lane | Subprocess | Dependency | Execution Mode |
|------------------|------------|------------|----------------|
| Lane_01 | SUB_01 | None | Synchronous |
| Lane_02 | SUB_02 | Conditional on insurance | Synchronous |
| Lane_03 | SUB_03 | Requires SUB_01/02 | Synchronous |
| Lane_04 | SUB_04 | Requires SUB_03 | Synchronous |
| Lane_05 | SUB_05 | Requires SUB_04 | Synchronous |
| Lane_06 | SUB_06 | Requires SUB_05 | Synchronous |
| Lane_07 | SUB_07 | Conditional on denials | Synchronous |
| Lane_08 | SUB_08 | Requires SUB_06 | Synchronous |
| Lane_09 | SUB_09 | Independent | **Asynchronous** |
| Lane_10 | SUB_10 | Independent | **Asynchronous** |

---

## 4. PARTICIPANT ARCHITECTURE

### 4.1 Collaboration Structure

```xml
<bpmn:collaboration id="Collaboration_Revenue_Cycle">
  <!-- Internal Pool (White Box) -->
  <bpmn:participant
    id="Participant_Hospital"
    name="Hospital - Ciclo de Receita"
    processRef="Process_ORCH_Revenue_Cycle" />

  <!-- External Pools (Black Box) -->
  <bpmn:participant
    id="Participant_Patient"
    name="Paciente / Responsável" />

  <bpmn:participant
    id="Participant_Insurance"
    name="Operadora de Saúde" />

  <bpmn:participant
    id="Participant_TASY"
    name="Sistema TASY ERP" />

  <bpmn:participant
    id="Participant_Government"
    name="Órgãos Reguladores (ANS/RF)" />

  <bpmn:participant
    id="Participant_Bank"
    name="Instituições Financeiras" />
</bpmn:collaboration>
```

### 4.2 Message Flow Matrix

| From | To | Message Type | Trigger | Subprocess |
|------|----|--------------|---------| -----------|
| Patient | Hospital | Appointment Request | Webhook/API | SUB_01 |
| Hospital | TASY | Patient Data | API Call | SUB_01 |
| Hospital | Insurance | Eligibility Check | RPA/API | SUB_02 |
| Insurance | Hospital | Authorization Response | Webhook | SUB_02 |
| Hospital | TASY | Admission Record | API Call | SUB_03 |
| Hospital | TASY | Clinical Data | API Call | SUB_04 |
| Hospital | Insurance | Billing Submit | Webservice | SUB_06 |
| Insurance | Hospital | Denial Notification | File/Portal | SUB_07 |
| Hospital | Insurance | Appeal Submission | RPA | SUB_07 |
| Bank | Hospital | Payment Notification | CNAB/PIX | SUB_08 |
| Hospital | Government | Compliance Report | Webservice | SUB_09 |

---

## 5. INTEGRATION ARCHITECTURE

### 5.1 Integration Points

```yaml
integration_landscape:
  tasy_erp:
    type: REST API + Database
    endpoints:
      - patient_management: /api/v1/patients
      - scheduling: /api/v1/appointments
      - admission: /api/v1/admissions
      - clinical_data: /api/v1/clinical
      - billing: /api/v1/billing
    authentication: OAuth 2.0
    rate_limit: 1000 req/min

  insurance_portals:
    type: RPA + Webservice
    providers:
      - Unimed: RPA (IBM RPA) + Webservice TISS
      - SulAmérica: RPA + Portal scraping
      - Bradesco: Webservice only
      - Amil: RPA + Webservice
    authentication: Per provider (varies)

  government:
    type: Webservice
    systems:
      - ANS: TISS XML submission
      - Receita Federal: CPF validation
    authentication: Digital certificate

  banking:
    type: API + File processing
    protocols:
      - PIX: API real-time
      - TED/DOC: CNAB 240/400 files
      - Credit card: Gateway API

  clinical_systems:
    lis:
      type: HL7 v2.5
      interface: MLLP
    pacs:
      type: DICOM + HL7
      interface: DICOMWeb

  iot_devices:
    rfid:
      type: MQTT
      devices: Material tracking
    weight_sensors:
      type: MQTT
      devices: Pharmacy dispensing
    biometric:
      type: REST API
      devices: Patient authentication
```

### 5.2 Connector Architecture

```yaml
camunda_connectors:
  tasy_connector:
    connector_id: "tasy-api-connector"
    implementation: "com.hospital.connectors.TASYConnector"
    configuration:
      base_url: "${tasyBaseUrl}"
      auth_type: "oauth2"
      timeout: 30000

  rpa_connector:
    connector_id: "ibm-rpa-connector"
    implementation: "com.ibm.rpa.camunda.RPAConnector"
    configuration:
      rpa_server: "${rpaServerUrl}"
      auth_token: "${rpaAuthToken}"
      async_mode: true

  llm_connector:
    connector_id: "llm-api-connector"
    implementation: "com.hospital.connectors.LLMConnector"
    configuration:
      provider: "openai" # or "azure", "anthropic"
      model: "gpt-4"
      max_tokens: 4000

  insurance_ws_connector:
    connector_id: "insurance-webservice-connector"
    implementation: "com.hospital.connectors.InsuranceWSConnector"
    configuration:
      soap_version: "1.2"
      wsdl_url: "${insuranceWSDL}"
      timeout: 60000
```

---

## 6. DATA ARCHITECTURE

### 6.1 Process Variables Schema

```typescript
// Global Process Variables (Orchestrator)
interface RevenueCycleVariables {
  // Business Key
  processBusinessKey: string; // Format: "YYYY-MM-DD-{patientId}-{sequence}"

  // Patient Data
  patientId: string;
  patientName: string;
  patientCPF: string;
  patientBirthDate: Date;

  // Insurance Data
  insuranceId?: string;
  insuranceName?: string;
  planCode?: string;
  cardNumber?: string;

  // Appointment Data
  appointmentId?: string;
  appointmentDateTime?: Date;
  serviceType: string;

  // Admission Data
  admissionId?: string;
  admissionDate?: Date;
  dischargeDate?: Date;

  // Clinical Data
  primaryDiagnosis?: string; // CID-10
  secondaryDiagnoses?: string[];
  procedures?: Array<{
    code: string; // TUSS
    description: string;
    quantity: number;
  }>;

  // Billing Data
  totalCharges?: number;
  authorizedAmount?: number;
  deniedAmount?: number;

  // Financial Data
  invoiceId?: string;
  paymentStatus?: string;
  receivedAmount?: number;

  // Flags
  hasInsurance: boolean;
  requiresAuthorization: boolean;
  hasGlosas: boolean;

  // Timestamps
  processStartDate: Date;
  processEndDate?: Date;
}
```

### 6.2 Subprocess-Specific Variables

See `/docs/architecture/conventions/VARIABLE_SCHEMAS.md` for complete schemas per subprocess.

### 6.3 Data Persistence

```yaml
persistence_strategy:
  process_engine:
    database: PostgreSQL 14+
    schema: camunda
    history_level: FULL
    history_time_to_live: P365D # 1 year

  business_data:
    primary: TASY ERP database
    backup: Data Lake (Parquet format)
    archival: Cold storage after 7 years

  documents:
    storage: S3-compatible object storage
    retention: Per document type
      - medical_records: 20 years
      - billing_docs: 10 years
      - audit_trails: 7 years
```

---

## 7. DEPLOYMENT ARCHITECTURE

### 7.1 Infrastructure Layout

```
┌─────────────────────────────────────────────────────────────┐
│                    Load Balancer (HAProxy)                   │
└─────────────────────────────────────────────────────────────┘
                           │
         ┌─────────────────┴─────────────────┐
         ↓                                   ↓
┌──────────────────┐              ┌──────────────────┐
│  Camunda Node 1  │              │  Camunda Node 2  │
│   (Primary)      │              │   (Standby)      │
└──────────────────┘              └──────────────────┘
         │                                   │
         └─────────────────┬─────────────────┘
                           ↓
         ┌─────────────────────────────────┐
         │  PostgreSQL Cluster (Primary +  │
         │  2 Read Replicas)               │
         └─────────────────────────────────┘
                           │
         ┌─────────────────┴─────────────────┐
         ↓                 ↓                 ↓
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│  RPA Bots    │  │  TASY ERP    │  │ External     │
│  (IBM RPA)   │  │  (Oracle)    │  │ Connectors   │
└──────────────┘  └──────────────┘  └──────────────┘
```

### 7.2 Environment Configuration

| Environment | Camunda Nodes | Database | Purpose |
|-------------|---------------|----------|---------|
| Development | 1 | PostgreSQL single | Development and unit testing |
| Staging | 2 | PostgreSQL HA | Integration testing |
| Production | 2 (active-standby) | PostgreSQL cluster | Live operations |
| DR | 1 (standby) | PostgreSQL replica | Disaster recovery |

### 7.3 Deployment Units

```yaml
deployment_artifacts:
  orchestrator:
    file: ORCH_Ciclo_Receita_Hospital_Futuro.bpmn
    deployment_name: "Revenue Cycle Orchestrator v1.0"
    dependencies: []

  subprocesses:
    - file: SUB_01_First_Contact.bpmn
      deployment_name: "First Contact Subprocess v1.0"
      dependencies: []

    - file: SUB_02_Pre_Authorization.bpmn
      deployment_name: "Pre-Authorization Subprocess v1.0"
      dependencies: [tasy-connector, rpa-connector]

    # ... (all 10 subprocesses)

  custom_code:
    - connector-implementations.jar
    - business-rules.jar
    - task-listeners.jar
```

---

## 8. SECURITY ARCHITECTURE

### 8.1 Security Layers

```yaml
security_controls:
  authentication:
    camunda_admin:
      method: LDAP/Active Directory
      mfa: Required

    service_accounts:
      method: OAuth 2.0 client credentials
      rotation: 90 days

  authorization:
    process_level:
      - Process definitions: Read-only for operators
      - Process instances: Based on tenant/department

    task_level:
      - User tasks: Role-based assignment
      - Service tasks: Execute as service account

    data_level:
      - Patient data: LGPD compliance
      - Financial data: PCI-DSS compliance

  encryption:
    at_rest:
      - Database: AES-256
      - File storage: AES-256
      - Backups: Encrypted

    in_transit:
      - API calls: TLS 1.3
      - Database connections: TLS 1.2+
      - Message flows: HTTPS/TLS

  audit:
    logging:
      - All process starts/ends
      - All user task assignments
      - All service task executions
      - All variable changes (sensitive data masked)

    retention: 7 years
```

### 8.2 Compliance Requirements

| Regulation | Requirements | Implementation |
|------------|-------------|----------------|
| LGPD | Data privacy, consent, portability | Consent subprocess, data encryption |
| ANS | TISS format, deadlines | Built into SUB_06, SUB_07 |
| CFM | Medical records retention | 20-year retention policy |
| PCI-DSS | Payment data security | Tokenization, encrypted storage |

---

## 9. MONITORING & OBSERVABILITY

### 9.1 Metrics Collection

```yaml
metrics:
  business_kpis:
    - name: authorization_approval_rate
      source: SUB_02
      calculation: approved / total_requests
      target: "> 85%"

    - name: denial_rate
      source: SUB_07
      calculation: denied_amount / billed_amount
      target: "< 5%"

    - name: collection_cycle_time
      source: SUB_08
      calculation: payment_date - billing_date
      target: "< 30 days"

    - name: coding_accuracy
      source: SUB_05
      calculation: correct_codes / total_codes
      target: "> 95%"

  technical_metrics:
    - name: process_cycle_time
      calculation: end_time - start_time
      target: "< 24 hours (p95)"

    - name: task_automation_rate
      calculation: automated_tasks / total_tasks
      target: "> 75%"

    - name: error_rate
      calculation: failed_instances / total_instances
      target: "< 2%"

    - name: connector_response_time
      calculation: Per connector average
      target: "< 5 seconds (p95)"
```

### 9.2 Alerting Strategy

```yaml
alerts:
  critical:
    - condition: "Process error rate > 5% in 15min"
      action: Page on-call engineer

    - condition: "ANS deadline < 4 hours"
      action: Email + Slack + SMS

    - condition: "Database connection pool exhausted"
      action: Auto-scale + Alert

  warning:
    - condition: "Authorization timeout > 48h"
      action: Email supervisor

    - condition: "Denial rate > 8%"
      action: Daily report to revenue manager

    - condition: "RPA bot failure"
      action: Retry + Alert if persistent
```

---

## 10. SCALABILITY & PERFORMANCE

### 10.1 Scaling Strategy

```yaml
horizontal_scaling:
  camunda_nodes:
    min: 2
    max: 4
    trigger: CPU > 70% or Queue depth > 1000

  database:
    read_replicas: 2
    write_node: 1 (with HA standby)

  job_executor:
    threads_per_node: 10
    max_jobs_per_acquisition: 3
    lock_time_in_millis: 300000 # 5 minutes

vertical_scaling:
  camunda_nodes:
    cpu: 4-8 cores
    memory: 8-16 GB
    jvm_heap: 6-12 GB

  database:
    cpu: 8-16 cores
    memory: 32-64 GB
    storage: SSD with 5000+ IOPS
```

### 10.2 Performance Targets

| Metric | Target | Max Acceptable |
|--------|--------|----------------|
| Process Start Time | < 1 sec | < 3 sec |
| User Task Load Time | < 2 sec | < 5 sec |
| Service Task Execution | < 5 sec | < 15 sec |
| Database Query Time | < 100ms | < 500ms |
| End-to-End Cycle Time | < 24h | < 48h |
| Concurrent Process Instances | 1000+ | 5000 |

---

## 11. DISASTER RECOVERY

### 11.1 Backup Strategy

```yaml
backups:
  database:
    full_backup: Daily at 2 AM
    incremental: Every 4 hours
    retention: 30 days
    location: Off-site S3

  process_definitions:
    version_control: Git repository
    backup: Automated on deploy
    retention: All versions

  business_documents:
    backup: Real-time replication
    retention: Per compliance requirements
```

### 11.2 Recovery Objectives

- **RTO (Recovery Time Objective)**: 4 hours
- **RPO (Recovery Point Objective)**: 1 hour
- **Failover Time**: < 15 minutes (automated)

---

## 12. EXTENSIBILITY & FUTURE ENHANCEMENTS

### 12.1 Planned Enhancements

1. **Value-Based Healthcare (VBHC)**
   - Outcome tracking subprocess
   - Risk stratification engine
   - Quality metrics integration

2. **Advanced AI/ML**
   - Predictive denial prevention
   - Automated clinical documentation improvement
   - Intelligent workload distribution

3. **Enhanced Patient Experience**
   - Real-time mobile notifications
   - Self-service portal expansion
   - Chatbot integration

4. **Financial Optimization**
   - Dynamic pricing engine
   - Bundled payment management
   - Automated contract negotiation

### 12.2 Extension Points

```yaml
extension_mechanisms:
  task_listeners:
    - BeforeTaskCreate
    - AfterTaskComplete
    - OnTaskAssignment

  execution_listeners:
    - ProcessStart
    - ProcessEnd
    - ActivityStart
    - ActivityEnd

  custom_batch_jobs:
    - Daily reconciliation
    - Weekly reporting
    - Monthly KPI calculation

  decision_tables:
    - Authorization requirements
    - Escalation rules
    - Pricing calculations
```

---

## 13. ARCHITECTURE DECISIONS

All architectural decisions are documented in `/docs/architecture/decisions/` using the ADR (Architecture Decision Record) format.

Key decisions:
- [ADR-001: Process Orchestration Pattern](./decisions/ADR-001-process-orchestration.md)
- [ADR-002: Integration Approach](./decisions/ADR-002-integration-approach.md)
- [ADR-003: Error Handling Strategy](./decisions/ADR-003-error-handling.md)
- [ADR-004: Data Persistence](./decisions/ADR-004-data-persistence.md)

---

## APPENDICES

### A. Glossary

- **TISS**: Troca de Informações na Saúde Suplementar (Health Insurance Data Exchange Standard)
- **ANS**: Agência Nacional de Saúde Suplementar (Brazilian Health Insurance Regulator)
- **TASY**: Hospital ERP system
- **LGPD**: Lei Geral de Proteção de Dados (Brazilian GDPR)
- **CID-10**: International Classification of Diseases, 10th revision
- **TUSS**: Terminologia Unificada da Saúde Suplementar (Unified Health Insurance Terminology)

### B. References

- BPMN 2.0 Specification: https://www.omg.org/spec/BPMN/2.0/
- Camunda 7 Documentation: https://docs.camunda.org/manual/7.18/
- TISS Standards: https://www.ans.gov.br/prestadores/tiss-troca-de-informacao-de-saude-suplementar

---

**Document Status**: APPROVED
**Next Review Date**: 2026-03-08
**Document Owner**: System Architect Team
