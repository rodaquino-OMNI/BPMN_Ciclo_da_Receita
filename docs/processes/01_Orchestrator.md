# ORCH - Main Orchestrator Process

## Overview

**Process ID**: `Process_ORCH_Revenue_Cycle`
**File**: `ORCH_Ciclo_Receita_Hospital_Futuro.bpmn`
**Purpose**: Orchestrates the complete revenue cycle from patient contact through financial closing

## Process Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        Hospital - Revenue Cycle                              │
├─────────────────────────────────────────────────────────────────────────────┤
│ Lane 1: First Contact                                                        │
│   [Start] → [SUB_01: First Contact] → [Gateway: Has Insurance?]             │
├─────────────────────────────────────────────────────────────────────────────┤
│ Lane 2: Pre-Authorization                                                    │
│              [SUB_02: Pre-Authorization] ← [Yes]                             │
│                        │                                                      │
│                        ▼                                                      │
├─────────────────────────────────────────────────────────────────────────────┤
│ Lane 3: Admission                                                            │
│              [SUB_03: Admission] ← [No - Private] ← [Gateway]               │
│                        │                                                      │
│                        ▼                                                      │
├─────────────────────────────────────────────────────────────────────────────┤
│ Lane 4: Clinical Production                                                  │
│              [SUB_04: Clinical Production]                                   │
│                        │                                                      │
│                        ▼                                                      │
├─────────────────────────────────────────────────────────────────────────────┤
│ Lane 5: Coding & Audit                                                       │
│              [SUB_05: Coding & Audit]                                        │
│                        │                                                      │
│                        ▼                                                      │
├─────────────────────────────────────────────────────────────────────────────┤
│ Lane 6: Billing & Submission                                                 │
│              [SUB_06: Billing & Submission] → [Gateway: Denied?]            │
├─────────────────────────────────────────────────────────────────────────────┤
│ Lane 7: Denials Management                                                   │
│              [SUB_07: Denials Mgmt] ← [Yes] → [Gateway: Resolved?]         │
│                                               [No] → [End: Write-off]        │
│                                               [Yes] ↓                        │
├─────────────────────────────────────────────────────────────────────────────┤
│ Lane 8: Collection                                                           │
│              [SUB_08: Collection] ← [No Denial]                             │
│                        │                                                      │
│                        ▼                                                      │
│              [Parallel Gateway: Split]                                       │
│                   │              │                                            │
├─────────────────────────────────────────────────────────────────────────────┤
│ Lane 9: Analytics       │                                                    │
│              [SUB_09: Analytics]                                             │
│                        │                                                      │
├─────────────────────────────────────────────────────────────────────────────┤
│ Lane 10: Maximization             │                                          │
│                        [SUB_10: Maximization]                                │
│                                    │                                          │
│              [Parallel Gateway: Join]                                        │
│                        │                                                      │
│                        ▼                                                      │
│                    [End: Complete]                                           │
└─────────────────────────────────────────────────────────────────────────────┘

External Participants:
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   Patient    │    │  Insurance   │    │     TASY     │
└──────────────┘    └──────────────┘    └──────────────┘
       │                   │                    │
       └───── Message Flows to/from Hospital ────┘
```

## Collaboration Structure

### Internal Participant

**Participant**: `Participant_Hospital`
**Name**: Hospital - Ciclo de Receita
**Process**: `Process_ORCH_Revenue_Cycle`

### External Participants (Black Box)

1. **Participant_Patient** - Paciente / Responsável
2. **Participant_Insurance** - Operadora de Saúde
3. **Participant_TASY** - Sistema TASY ERP
4. **Participant_Government** - Órgãos Reguladores (ANS/RF)
5. **Participant_Bank** - Instituições Financeiras

## Lanes Description

### Lane 1: First Contact / Scheduling
**ID**: `Lane_01_First_Contact`
**Color**: #E8F6F3 (light green)
**Purpose**: Initial patient contact and appointment scheduling

**Elements**:
- Start Event: Patient contact received
- Call Activity: SUB_01_First_Contact
- Gateway: Check if patient has insurance

### Lane 2: Pre-Authorization / Eligibility
**ID**: `Lane_02_Pre_Authorization`
**Color**: #EBF5FB (light blue)
**Purpose**: Insurance verification and authorization

**Elements**:
- Call Activity: SUB_02_Pre_Authorization
- Timer Boundary Event: 48-hour authorization timeout

### Lane 3: Admission / Registration
**ID**: `Lane_03_Admission`
**Color**: #F4ECF7 (light purple)
**Purpose**: Patient admission and registration

**Elements**:
- Call Activity: SUB_03_Admission
- Subprocess: Emergency admission (event-based)

### Lane 4: Clinical Production
**ID**: `Lane_04_Clinical_Production`
**Color**: #FEF9E7 (light yellow)
**Purpose**: Clinical care delivery and documentation

**Elements**:
- Call Activity: SUB_04_Clinical_Production
- Signal Boundary Event: Patient discharge

### Lane 5: Coding / Audit
**ID**: `Lane_05_Coding_Audit`
**Color**: #FDEDEC (light red)
**Purpose**: Medical coding and internal audit

**Elements**:
- Call Activity: SUB_05_Coding_Audit

### Lane 6: Billing / Submission
**ID**: `Lane_06_Billing_Submission`
**Color**: #F0F3F4 (light gray)
**Purpose**: Invoice generation and submission

**Elements**:
- Call Activity: SUB_06_Billing_Submission
- Error Boundary Event: Transmission failure
- Gateway: Check for denials

### Lane 7: Denials Management
**ID**: `Lane_07_Denials_Management`
**Color**: #FDF2E9 (light orange)
**Purpose**: Managing denied claims and appeals

**Elements**:
- Call Activity: SUB_07_Denials_Management
- Escalation Boundary Event: ANS deadline critical
- Gateway: Check resolution status

### Lane 8: Revenue Collection
**ID**: `Lane_08_Revenue_Collection`
**Color**: #E8F8F5 (light teal)
**Purpose**: Payment processing and reconciliation

**Elements**:
- Call Activity: SUB_08_Revenue_Collection
- Parallel Gateway (Split): Fork to analytics and maximization

### Lane 9: Analytics / BI
**ID**: `Lane_09_Analytics`
**Color**: #EAF2F8 (light blue-gray)
**Purpose**: Real-time analytics and KPI tracking

**Elements**:
- Call Activity: SUB_09_Analytics (asynchronous)

### Lane 10: Revenue Maximization
**ID**: `Lane_10_Maximization`
**Color**: #F5EEF8 (light lavender)
**Purpose**: Continuous improvement and optimization

**Elements**:
- Call Activity: SUB_10_Maximization (asynchronous)
- Parallel Gateway (Join): Converge parallel paths

## Process Flow Logic

### Main Path

1. **Start Event** → Patient contact received via any channel
2. **Call SUB_01** → First contact and scheduling process
3. **Insurance Gateway** → Check if patient has insurance
   - **YES** → Route to Pre-Authorization (SUB_02)
   - **NO** → Route directly to Admission (SUB_03) as private patient

4. **Call SUB_02** (if insurance) → Pre-authorization process
   - Boundary: 48-hour timer for timeout
   - On timeout: Escalate to manual intervention

5. **Call SUB_03** → Admission and registration
   - Event subprocess: Emergency admission shortcut

6. **Call SUB_04** → Clinical production
   - Boundary: Discharge signal event
   - Concurrent: Audit subprocess runs in parallel

7. **Call SUB_05** → Coding and internal audit

8. **Call SUB_06** → Billing and submission
   - Boundary: Error event for transmission failure
   - On error: Retry mechanism activated

9. **Denial Gateway** → Check if claims were denied
   - **YES** → Route to Denials Management (SUB_07)
   - **NO** → Route to Collection (SUB_08)

10. **Call SUB_07** (if denied) → Denials management
    - Boundary: Escalation for ANS deadline
    - Resolution gateway:
      - **Resolved** → Return to billing or continue to collection
      - **Unresolved** → End with write-off

11. **Call SUB_08** → Revenue collection and reconciliation

12. **Parallel Split** → Fork execution
    - Path A: Analytics (SUB_09)
    - Path B: Maximization (SUB_10)

13. **Parallel Join** → Wait for both paths to complete

14. **End Event** → Revenue cycle complete

## Call Activity Configuration

### Standard Pattern

All call activities follow this pattern:

```xml
<bpmn:callActivity id="CallActivity_SUB_XX"
                   name="Subprocess Name"
                   calledElement="Process_SUB_XX_Name"
                   camunda:asyncBefore="true">
  <bpmn:extensionElements>
    <camunda:in businessKey="#{execution.processBusinessKey}" />
    <camunda:in variables="all" />
    <camunda:out variables="all" />
  </bpmn:extensionElements>
  <bpmn:incoming>Flow_input</bpmn:incoming>
  <bpmn:outgoing>Flow_output</bpmn:outgoing>
</bpmn:callActivity>
```

### Asynchronous Call Activities

SUB_09 and SUB_10 are marked as asynchronous:

```xml
<bpmn:callActivity id="CallActivity_SUB_09"
                   camunda:asyncBefore="true"
                   camunda:exclusive="false">
```

## Boundary Events

### Timer Event - Authorization Timeout

**Attached to**: `CallActivity_SUB_02`
**Type**: Non-interrupting Timer
**Duration**: PT48H (48 hours)
**Action**: Escalate to manager, send notification

```xml
<bpmn:boundaryEvent id="Event_Auth_Timeout"
                    cancelActivity="false"
                    attachedToRef="CallActivity_SUB_02">
  <bpmn:timerEventDefinition>
    <bpmn:timeDuration>PT48H</bpmn:timeDuration>
  </bpmn:timerEventDefinition>
</bpmn:boundaryEvent>
```

### Error Event - Transmission Failure

**Attached to**: `CallActivity_SUB_06`
**Type**: Interrupting Error
**Error Code**: `Error_Transmission`
**Action**: Route to retry logic

```xml
<bpmn:boundaryEvent id="Event_Transmission_Error"
                    cancelActivity="true"
                    attachedToRef="CallActivity_SUB_06">
  <bpmn:errorEventDefinition errorRef="Error_Transmission" />
</bpmn:boundaryEvent>
```

### Signal Event - Patient Discharge

**Attached to**: `CallActivity_SUB_04`
**Type**: Interrupting Signal
**Signal**: `Signal_Discharge`
**Action**: Accelerate to billing

```xml
<bpmn:boundaryEvent id="Event_Discharge"
                    cancelActivity="true"
                    attachedToRef="CallActivity_SUB_04">
  <bpmn:signalEventDefinition signalRef="Signal_Discharge" />
</bpmn:boundaryEvent>
```

### Escalation Event - ANS Deadline

**Attached to**: `CallActivity_SUB_07`
**Type**: Non-interrupting Escalation
**Escalation Code**: `Escalation_ANS_Deadline`
**Action**: Alert management, increase priority

```xml
<bpmn:boundaryEvent id="Event_ANS_Escalation"
                    cancelActivity="false"
                    attachedToRef="CallActivity_SUB_07">
  <bpmn:escalationEventDefinition escalationRef="Escalation_ANS_Deadline" />
</bpmn:boundaryEvent>
```

## Gateway Logic

### Gateway 1: Has Insurance?

**ID**: `Gateway_Has_Insurance`
**Type**: Exclusive Gateway (XOR)
**Condition Variable**: `hasInsurance`

**Outgoing Flows**:
```xml
<bpmn:sequenceFlow id="Flow_To_Auth" sourceRef="Gateway_Has_Insurance"
                   targetRef="CallActivity_SUB_02">
  <bpmn:conditionExpression xsi:type="bpmn:tFormalExpression">
    ${hasInsurance == true}
  </bpmn:conditionExpression>
</bpmn:sequenceFlow>

<bpmn:sequenceFlow id="Flow_To_Admission" sourceRef="Gateway_Has_Insurance"
                   targetRef="CallActivity_SUB_03">
  <bpmn:conditionExpression xsi:type="bpmn:tFormalExpression">
    ${hasInsurance == false}
  </bpmn:conditionExpression>
</bpmn:sequenceFlow>
```

### Gateway 2: Denied?

**ID**: `Gateway_Denied`
**Type**: Exclusive Gateway (XOR)
**Condition Variable**: `hasDenials`

**Outgoing Flows**:
```xml
<bpmn:sequenceFlow id="Flow_To_Denials" sourceRef="Gateway_Denied"
                   targetRef="CallActivity_SUB_07">
  <bpmn:conditionExpression xsi:type="bpmn:tFormalExpression">
    ${hasDenials == true}
  </bpmn:conditionExpression>
</bpmn:sequenceFlow>

<bpmn:sequenceFlow id="Flow_To_Collection" sourceRef="Gateway_Denied"
                   targetRef="CallActivity_SUB_08">
  <bpmn:conditionExpression xsi:type="bpmn:tFormalExpression">
    ${hasDenials == false}
  </bpmn:conditionExpression>
</bpmn:sequenceFlow>
```

### Gateway 3: Parallel Split

**ID**: `Gateway_Parallel_Split`
**Type**: Parallel Gateway (AND)
**Purpose**: Execute analytics and maximization concurrently

```xml
<bpmn:parallelGateway id="Gateway_Parallel_Split" />
```

### Gateway 4: Parallel Join

**ID**: `Gateway_Parallel_Join`
**Type**: Parallel Gateway (AND)
**Purpose**: Wait for both async processes to complete

```xml
<bpmn:parallelGateway id="Gateway_Parallel_Join" />
```

## Message Flows

### Inbound Messages

1. **Patient Request** → Start Event
   - From: Participant_Patient
   - To: Event_Start_Patient_Contact
   - Trigger: WhatsApp/Portal/App/Phone

2. **Authorization Response** → SUB_02
   - From: Participant_Insurance
   - To: Task_Receive_Auth_Response
   - Content: Authorization number or denial

3. **Payment Notification** → SUB_08
   - From: Participant_Bank
   - To: Event_Payment_Received
   - Content: CNAB file or PIX notification

### Outbound Messages

1. **Authorization Request** → Insurance
   - From: Task_Submit_Authorization
   - To: Participant_Insurance
   - Content: TISS guide XML

2. **Billing Submission** → Insurance
   - From: Task_Submit_Billing
   - To: Participant_Insurance
   - Content: TISS batch XML

3. **Compliance Report** → Government
   - From: Task_Report_Compliance
   - To: Participant_Government
   - Content: ANS reporting data

## Process Variables

### Core Variables

| Variable | Type | Source | Description |
|----------|------|--------|-------------|
| `patientId` | String | SUB_01 | Unique patient identifier |
| `accountId` | String | SUB_03 | Account/admission number |
| `hasInsurance` | Boolean | SUB_01 | Patient has insurance coverage |
| `insuranceId` | String | SUB_01 | Insurance company identifier |
| `authorizationNumber` | String | SUB_02 | Authorization number from payer |
| `hasDenials` | Boolean | SUB_06 | Claims have denials |
| `totalAmount` | Double | SUB_06 | Total billed amount |
| `collectedAmount` | Double | SUB_08 | Amount collected |
| `denialAmount` | Double | SUB_07 | Amount denied |
| `processStatus` | String | All | Current status of revenue cycle |

### Status Values

```java
public enum ProcessStatus {
    SCHEDULED,           // After SUB_01
    AUTHORIZED,          // After SUB_02
    ADMITTED,            // After SUB_03
    IN_TREATMENT,        // During SUB_04
    DISCHARGED,          // After SUB_04
    CODED,               // After SUB_05
    BILLED,              // After SUB_06
    DENIED,              // If SUB_07 triggered
    APPEALED,            // During SUB_07
    COLLECTED,           // After SUB_08
    COMPLETED            // End state
}
```

## Performance Characteristics

### Execution Metrics

- **Average Duration**: 3-30 days (depending on care type)
- **Active Instances**: 5,000-20,000 concurrent
- **Completion Rate**: 98.5%
- **Exception Rate**: 1.5%

### Resource Utilization

- **Job Executor Threads**: 10 per node
- **Async Jobs**: Prioritized (high priority for SUB_02, SUB_06)
- **History Level**: FULL (365 days retention)
- **Incident Handling**: Automatic retry with backoff

## Monitoring and KPIs

### Process KPIs

1. **Cycle Time**: Time from first contact to payment
2. **Authorization Rate**: Percentage of authorizations approved
3. **Denial Rate**: Percentage of claims denied
4. **Collection Rate**: Percentage of billed amount collected
5. **Days in AR**: Average days to collect payment

### Alerts

- Authorization timeout (> 48 hours)
- Transmission failures
- High denial rate (> 15%)
- Payment delays (> 30 days)
- Process instance failures

### Cockpit Dashboard

Custom dashboard showing:
- Active instances by subprocess
- SLA compliance by subprocess
- Real-time denial rate
- Collection rate trend
- Revenue forecast

## Error Handling

### Retry Strategy

- **Transmission Errors**: 3 retries with exponential backoff
- **Integration Failures**: Circuit breaker pattern
- **Timeout Handling**: Escalation to manual intervention

### Incidents

Automatic incident creation for:
- Failed service tasks
- Unhandled exceptions
- External service timeouts
- Data validation failures

## Testing Scenarios

### Happy Path

1. Patient calls → Schedule → Authorize → Admit → Treat → Code → Bill → Collect → Complete

### Alternative Paths

1. Private patient (no authorization)
2. Authorization denied → Appeal → Retry
3. Billing denied → Appeals process
4. Payment delay → Collection workflow

### Exception Scenarios

1. Authorization timeout
2. Transmission failure
3. Emergency admission
4. Partial denials
5. Payment matching failures

## Integration Points

- **TASY ERP**: All subprocesses
- **Insurance Portals**: SUB_02, SUB_06, SUB_07
- **RPA Platform**: SUB_02, SUB_06, SUB_07, SUB_08
- **LLM Services**: SUB_05, SUB_07, SUB_10
- **Analytics Platform**: SUB_09
- **Banking APIs**: SUB_08

---

**Next**: [SUB_01 - First Contact Process](./02_First_Contact.md)

**Last Updated**: 2025-12-08
