# BPMN Process Overview

## Introduction

The Hospital Revenue Cycle is implemented as a sophisticated orchestration of 11 interconnected BPMN processes that manage the complete patient journey from first contact through payment collection and continuous improvement.

## Process Architecture

### Orchestration Model

```
┌─────────────────────────────────────────────────────────────────┐
│         ORCH_Ciclo_Receita_Hospital_Futuro (Orchestrator)       │
│                                                                  │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐ │
│  │  SUB_01  │───▶│  SUB_02  │───▶│  SUB_03  │───▶│  SUB_04  │ │
│  │  Contact │    │   Auth   │    │Admission │    │ Clinical │ │
│  └──────────┘    └──────────┘    └──────────┘    └──────────┘ │
│                                                          │       │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐         │       │
│  │  SUB_05  │◀───│  SUB_04  │    │  SUB_06  │◀────────┘       │
│  │  Coding  │───▶│          │───▶│  Billing │                 │
│  └──────────┘    └──────────┘    └──────────┘                 │
│                                          │                       │
│  ┌──────────┐                           │                       │
│  │  SUB_07  │◀──────────────────────────┘                      │
│  │ Denials  │                                                   │
│  └──────────┘                                                   │
│       │                                                          │
│       ▼                                                          │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐                 │
│  │  SUB_08  │───▶│  SUB_09  │    │  SUB_10  │                 │
│  │Collection│    │Analytics │    │ Maximize │                 │
│  └──────────┘    └──────────┘    └──────────┘                 │
└─────────────────────────────────────────────────────────────────┘
```

## Process Catalog

### 1. ORCH - Main Orchestrator
**File**: `ORCH_Ciclo_Receita_Hospital_Futuro.bpmn`
**Purpose**: Coordinates all 10 subprocesses in the revenue cycle
**Trigger**: Patient contact event
**Duration**: Variable (hours to months depending on care)

**Key Features**:
- Call activity orchestration
- Parallel execution where appropriate
- Exception handling with boundary events
- Message flows to external participants

### 2. SUB_01 - First Contact and Scheduling
**File**: `SUB_01_First_Contact.bpmn`
**Purpose**: Multi-channel patient contact and appointment scheduling
**Trigger**: Message event (WhatsApp/Portal/Phone/App)
**Duration**: 3-5 minutes average

**Key Features**:
- Multi-channel support (WhatsApp, Portal, App, Phone)
- Automated patient identification
- AI-powered service classification
- Real-time availability checking
- Automated confirmations and reminders

### 3. SUB_02 - Pre-Authorization and Eligibility
**File**: `SUB_02_Pre_Authorization.bpmn`
**Purpose**: Insurance eligibility verification and procedure authorization
**Trigger**: Scheduled appointment with insurance
**Duration**: 24-48 hours

**Key Features**:
- RPA-powered portal automation
- Real-time eligibility verification
- TISS guide generation
- Automated authorization submission
- LLM-powered appeal generation for denials
- ANS deadline tracking

### 4. SUB_03 - Admission and Registration
**File**: `SUB_03_Admission.bpmn`
**Purpose**: Patient check-in and admission to facility
**Trigger**: Patient arrival
**Duration**: 10-15 minutes

**Key Features**:
- Self-service kiosk support
- Biometric authentication
- OCR document digitization
- CPF validation with government
- Credit scoring integration
- Digital consent capture
- RFID bracelet generation

### 5. SUB_04 - Clinical Production and Documentation
**File**: `SUB_04_Clinical_Production.bpmn`
**Purpose**: Clinical care delivery and documentation
**Trigger**: Patient admission complete
**Duration**: Hours to days

**Key Features**:
- Electronic health record integration
- IoT/RFID material tracking
- Automated medication administration
- LIS/PACS integration
- Concurrent audit subprocess
- Discharge signal event

### 6. SUB_05 - Coding and Internal Audit
**File**: `SUB_05_Coding_Audit.bpmn`
**Purpose**: Medical coding and quality audit
**Trigger**: Clinical documentation complete
**Duration**: 2-4 hours

**Key Features**:
- AI-powered TUSS code suggestion
- Automated DRG coding
- CID-procedure validation
- Confidence-based human review routing
- Completeness checking
- Quality scoring

### 7. SUB_06 - Billing and Submission
**File**: `SUB_06_Billing_Submission.bpmn`
**Purpose**: Invoice generation and submission to payers
**Trigger**: Coding complete
**Duration**: 12-24 hours

**Key Features**:
- Charge consolidation
- Contract rules application
- TISS XML generation
- Multi-channel submission (webservice/portal)
- Protocol capture
- Automatic retry on failure

### 8. SUB_07 - Denials Management
**File**: `SUB_07_Denials_Management.bpmn`
**Purpose**: Handling denied claims and appeals
**Trigger**: Denial notification from payer
**Duration**: 5-7 days

**Key Features**:
- RPA denial capture
- AI-powered denial classification
- Automatic correction for simple denials
- LLM-generated appeals
- Evidence gathering
- ANS deadline tracking
- Escalation management

### 9. SUB_08 - Revenue Collection and Reconciliation
**File**: `SUB_08_Revenue_Collection.bpmn`
**Purpose**: Payment processing and financial reconciliation
**Trigger**: Daily timer (06:00)
**Duration**: Continuous (daily cycles)

**Key Features**:
- CNAB file processing
- PIX integration
- Automated payment matching
- Aging analysis
- Collection workflow
- Credit bureau integration
- Legal referral automation

### 10. SUB_09 - Analytics and Business Intelligence
**File**: `SUB_09_Analytics.bpmn`
**Purpose**: Real-time analytics and KPI calculation
**Trigger**: Timer (every 5 minutes)
**Duration**: Continuous

**Key Features**:
- Multi-source data collection
- Stream processing
- KPI calculation
- ML anomaly detection
- Predictive analytics
- Dashboard updates
- Automated alerts

### 11. SUB_10 - Revenue Maximization
**File**: `SUB_10_Maximization.bpmn`
**Purpose**: Continuous improvement and revenue optimization
**Trigger**: Weekly timer
**Duration**: Continuous

**Key Features**:
- Upsell opportunity identification
- Undercoding detection
- Missed charge detection
- Benchmark analysis
- Process mining
- Bottleneck identification
- Action plan generation

## Process Interactions

### Synchronous Flows

```
First Contact → Pre-Authorization → Admission → Clinical → Coding → Billing
```

These processes execute sequentially with the orchestrator waiting for each to complete.

### Asynchronous Flows

```
Collection → [Analytics ∥ Maximization]
```

Analytics and Maximization run in parallel and continuously, independent of individual patient flows.

### Exception Flows

```
Billing → Denials → (back to Billing or Collection)
```

Denials can interrupt the normal flow and create loops back to billing.

## Common Patterns

### Call Activities

All subprocesses are invoked via Call Activities:

```xml
<bpmn:callActivity id="CallActivity_SUB_XX"
                   name="Subprocess Name"
                   calledElement="Process_SUB_XX_Name">
  <bpmn:extensionElements>
    <camunda:in businessKey="#{execution.processBusinessKey}" />
    <camunda:in variables="all" />
    <camunda:out variables="all" />
  </bpmn:extensionElements>
</bpmn:callActivity>
```

### Boundary Events

Common boundary events used:

- **Timer**: Timeouts (authorization, ANS deadlines)
- **Error**: Technical failures (submission errors)
- **Signal**: Business events (discharge, emergency)
- **Escalation**: Deadline escalations

### Message Flows

External participants communicate via message flows:

- Patient → Hospital: Appointment requests
- Hospital → Insurance: Authorization requests
- Insurance → Hospital: Authorization responses
- Hospital → Government: Compliance reporting
- Bank → Hospital: Payment notifications

## Lane Structure

### Common Lane Types

1. **User Lanes**: Manual tasks requiring human interaction
2. **Service Lanes**: Automated tasks with system integration
3. **RPA Lanes**: Robotic process automation tasks
4. **AI/ML Lanes**: Artificial intelligence powered tasks
5. **Integration Lanes**: External system communication

### Lane Organization

Each subprocess organizes lanes by:
- **Role** (Reception, Medical, Billing)
- **System** (TASY, RPA, AI)
- **Function** (Authorization, Audit, Collection)

## Execution Characteristics

### Performance Targets

| Process | Target Duration | SLA |
|---------|----------------|-----|
| SUB_01 First Contact | 5 minutes | 15 minutes |
| SUB_02 Pre-Authorization | 24 hours | 48 hours |
| SUB_03 Admission | 15 minutes | 30 minutes |
| SUB_04 Clinical | Variable | N/A |
| SUB_05 Coding | 4 hours | 24 hours |
| SUB_06 Billing | 12 hours | 24 hours |
| SUB_07 Denials | 5 days | 7 days |
| SUB_08 Collection | Daily | Daily |
| SUB_09 Analytics | 5 minutes | Real-time |
| SUB_10 Maximization | Weekly | Monthly |

### Scalability

- **Horizontal Scaling**: All processes support clustering
- **Async Execution**: Service tasks use async before/after
- **Job Prioritization**: Critical paths have higher priority
- **Resource Optimization**: Automated resource allocation

## Monitoring and Observability

### KPIs Tracked

- Process instance count by status
- Average duration per process
- Task completion times
- Failure rates and causes
- Business outcomes (authorization rate, denial rate, collection rate)

### Cockpit Views

Standard Camunda Cockpit views plus:
- Custom revenue cycle dashboard
- Real-time process heatmap
- SLA compliance tracking
- Financial metrics overview

## Next Steps

For detailed documentation of each process:

1. [ORCH - Main Orchestrator](./01_Orchestrator.md)
2. [SUB_01 - First Contact](./02_First_Contact.md)
3. [SUB_02 - Pre-Authorization](./03_Pre_Authorization.md)
4. [SUB_03 - Admission](./04_Admission.md)
5. [SUB_04 - Clinical Production](./05_Clinical_Production.md)
6. [SUB_05 - Coding and Audit](./06_Coding_Audit.md)
7. [SUB_06 - Billing Submission](./07_Billing_Submission.md)
8. [SUB_07 - Denials Management](./08_Denials_Management.md)
9. [SUB_08 - Revenue Collection](./09_Revenue_Collection.md)
10. [SUB_09 - Analytics](./10_Analytics.md)
11. [SUB_10 - Maximization](./11_Maximization.md)

---

**Last Updated**: 2025-12-08
