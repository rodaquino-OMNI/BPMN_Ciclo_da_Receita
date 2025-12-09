# BPMN Validation Checklist
**Comprehensive Quality Control Matrix**

---

## ORCHESTRATOR: ORCH_Ciclo_Receita_Hospital_Futuro.bpmn

### File Existence
- [ ] File exists at `/src/bpmn/ORCH_Ciclo_Receita_Hospital_Futuro.bpmn`
- [ ] File is valid XML
- [ ] File size > 10KB (minimum for complete orchestrator)

### XML Structure
- [ ] XML declaration present: `<?xml version="1.0" encoding="UTF-8"?>`
- [ ] Root element: `<bpmn:definitions>`
- [ ] All required namespaces declared:
  - [ ] `xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"`
  - [ ] `xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"`
  - [ ] `xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"`
  - [ ] `xmlns:di="http://www.omg.org/spec/DD/20100524/DI"`
  - [ ] `xmlns:camunda="http://camunda.org/schema/1.0/bpmn"`
  - [ ] `xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"`
- [ ] Unique definitions ID
- [ ] Target namespace defined
- [ ] Exporter: "Camunda Modeler"

### Collaboration Structure
- [ ] Collaboration element exists
- [ ] Collaboration has unique ID: `Collaboration_Revenue_Cycle_Orchestrator`

### Participants (6 Required)
- [ ] Participant_Hospital (with processRef)
- [ ] Participant_Patient (black box)
- [ ] Participant_Insurance (black box)
- [ ] Participant_TASY (black box)
- [ ] Participant_Government (black box)
- [ ] Participant_Bank (black box)

### Message Flows (Minimum 8 Required)
- [ ] MsgFlow from Patient to Event_Start_Patient_Contact
- [ ] MsgFlow from Hospital to Insurance
- [ ] MsgFlow from Insurance to Hospital
- [ ] MsgFlow from Hospital to TASY
- [ ] MsgFlow from TASY to Hospital
- [ ] MsgFlow from Hospital to Bank
- [ ] MsgFlow from Hospital to Government
- [ ] All message flows have unique IDs following pattern

### Process Element
- [ ] Process ID: `Process_ORCH_Revenue_Cycle`
- [ ] `isExecutable="true"`
- [ ] `camunda:historyTimeToLive` defined (P365D or 730)

### Lanes (10 Required)
- [ ] Lane_01_First_Contact
- [ ] Lane_02_Pre_Authorization
- [ ] Lane_03_Admission
- [ ] Lane_04_Clinical_Production
- [ ] Lane_05_Coding_Audit
- [ ] Lane_06_Billing_Submission
- [ ] Lane_07_Denials_Management
- [ ] Lane_08_Revenue_Collection
- [ ] Lane_09_Analytics
- [ ] Lane_10_Maximization
- [ ] Each lane has `<bpmn:flowNodeRef>` for all contained elements

### Events
- [ ] Start Event exists: `Event_Start_Patient_Contact`
- [ ] Start Event is Message Event
- [ ] End Event exists: `Event_End_Cycle_Complete`
- [ ] Boundary Timer Event on SUB_02 (48h timeout)
- [ ] Boundary Error Event on SUB_06
- [ ] Boundary Signal Event on SUB_04
- [ ] Boundary Escalation Event on SUB_07

### Call Activities (10 Required)
- [ ] CallActivity_SUB_01 (calledElement: Process_SUB_01_First_Contact)
- [ ] CallActivity_SUB_02 (calledElement: Process_SUB_02_Pre_Authorization)
- [ ] CallActivity_SUB_03 (calledElement: Process_SUB_03_Admission)
- [ ] CallActivity_SUB_04 (calledElement: Process_SUB_04_Clinical_Production)
- [ ] CallActivity_SUB_05 (calledElement: Process_SUB_05_Coding_Audit)
- [ ] CallActivity_SUB_06 (calledElement: Process_SUB_06_Billing_Submission)
- [ ] CallActivity_SUB_07 (calledElement: Process_SUB_07_Denials_Management)
- [ ] CallActivity_SUB_08 (calledElement: Process_SUB_08_Revenue_Collection)
- [ ] CallActivity_SUB_09 (calledElement: Process_SUB_09_Analytics)
- [ ] CallActivity_SUB_10 (calledElement: Process_SUB_10_Maximization)

### Call Activity Configuration
- [ ] All have `<camunda:in businessKey="#{execution.processBusinessKey}" />`
- [ ] All have `<camunda:in variables="all" />`
- [ ] All have `<camunda:out variables="all" />`

### Gateways
- [ ] Gateway_Insurance_Check (Exclusive) - "Tem Convênio?"
- [ ] Gateway_Denial_Check (Exclusive) - "Houve Glosa?"
- [ ] Gateway_Parallel_Split (Parallel) - Split for SUB_09 and SUB_10
- [ ] Gateway_Parallel_Join (Parallel) - Join after SUB_09 and SUB_10
- [ ] All gateways have conditions using `${}` syntax

### Sequence Flows
- [ ] All flow nodes properly connected
- [ ] All flows have unique IDs following pattern `Flow_[Source]_to_[Target]`
- [ ] All flows have sourceRef and targetRef
- [ ] Gateway conditions properly defined
- [ ] No orphaned elements

### Visual Elements - BPMNDiagram
- [ ] BPMNDiagram element exists
- [ ] BPMNPlane element exists with correct bpmnElement reference
- [ ] All 6 participants have BPMNShape
- [ ] All 10 lanes have BPMNShape with isHorizontal="true"
- [ ] All tasks have BPMNShape (dimensions 100x80)
- [ ] All gateways have BPMNShape (dimensions 50x50)
- [ ] All events have BPMNShape (dimensions 36x36)
- [ ] All sequence flows have BPMNEdge with waypoints
- [ ] All message flows have BPMNEdge with waypoints
- [ ] No overlapping elements
- [ ] Proper spacing (150-200px horizontal, 100-150px vertical)
- [ ] Labels don't overlap elements

### Camunda Modeler Test
- [ ] Opens without errors
- [ ] All elements render correctly
- [ ] All lanes visible
- [ ] All flows connected properly
- [ ] Labels readable
- [ ] Can export as PNG/SVG

### Deployment Test
- [ ] Deploys to Camunda Engine without errors
- [ ] Process definition visible in Cockpit
- [ ] Can start process instance
- [ ] No validation warnings

---

## SUB_01: First Contact and Scheduling

### File Existence
- [ ] File exists at `/src/bpmn/SUB_01_First_Contact.bpmn`
- [ ] Valid XML structure
- [ ] File size > 8KB

### Process Configuration
- [ ] Process ID: `Process_SUB_01_First_Contact`
- [ ] `isExecutable="true"`
- [ ] `camunda:historyTimeToLive` defined

### Lanes (3 Required)
- [ ] Lane_Digital_Channels
- [ ] Lane_Call_Center
- [ ] Lane_TASY_Scheduling

### Elements Count
- [ ] 1 Message Start Event
- [ ] 8 Service Tasks (minimum)
- [ ] 2 User Tasks
- [ ] 2 Exclusive Gateways
- [ ] 1 Timer Boundary Event
- [ ] 1 End Event

### Critical Service Tasks
- [ ] Task_Identify_Channel (with RPA classification)
- [ ] Task_Create_Patient (API: TASY Patient Create)
- [ ] Task_Identify_Service (AI: NLP)
- [ ] Task_Check_Availability (API: TASY Schedule Query)
- [ ] Task_Add_Waiting_List (API: TASY Waiting List)
- [ ] Task_Book_Appointment (API: TASY Schedule Book)
- [ ] Task_Send_Confirmation (RPA: multicanal)
- [ ] Task_Send_Reminder (RPA: multicanal)
- [ ] All service tasks have `camunda:asyncBefore="true"`

### Variables
- [ ] patientId defined
- [ ] patientName defined
- [ ] patientCPF defined
- [ ] contactChannel defined
- [ ] serviceType defined
- [ ] appointmentDateTime defined
- [ ] slotId defined
- [ ] insuranceId defined
- [ ] appointmentId defined

### Visual Elements
- [ ] Complete BPMNDiagram section
- [ ] All elements have shapes
- [ ] All flows have edges
- [ ] Proper positioning
- [ ] No overlaps

### Camunda Test
- [ ] Renders in Modeler
- [ ] Deploys to Engine
- [ ] Can be called from orchestrator

---

## SUB_02: Pre-Authorization and Eligibility

### File Existence
- [ ] File exists at `/src/bpmn/SUB_02_Pre_Authorization.bpmn`
- [ ] Valid XML structure
- [ ] File size > 10KB

### Process Configuration
- [ ] Process ID: `Process_SUB_02_Pre_Authorization`
- [ ] `isExecutable="true"`
- [ ] `camunda:historyTimeToLive` defined

### Lanes (4 Required)
- [ ] Lane_Eligibility
- [ ] Lane_Authorization
- [ ] Lane_RPA_Portals
- [ ] Lane_Appeals

### Elements Count
- [ ] 1 Start Event
- [ ] 9 Service Tasks (minimum)
- [ ] 2 Exclusive Gateways
- [ ] 1 Timer Boundary Event (PT48H)
- [ ] 2 End Events (Authorized, Denied)

### Critical Service Tasks with Connectors
- [ ] Task_TASY_Eligibility (API integration)
- [ ] Task_RPA_Portal_Check (IBM RPA with connector)
  - [ ] Connector has `camunda:connectorId`
  - [ ] Input parameters defined
  - [ ] Output mapping defined
- [ ] Task_Calculate_Copay (Rules Engine)
- [ ] Task_Generate_TISS (API: TASY TISS Generator)
- [ ] Task_Submit_Auth (RPA: portal/webservice)
- [ ] Task_LLM_Appeal (API: LLM Analysis)
- [ ] Task_Submit_Appeal (RPA: submit recurso)
- [ ] Task_Update_Status (API: TASY Auth Update)

### Variables (14 Required)
- [ ] insuranceId
- [ ] insuranceName
- [ ] planCode
- [ ] procedureCodes
- [ ] isEligible
- [ ] copayAmount
- [ ] tissGuideNumber
- [ ] authorizationNumber
- [ ] authStatus
- [ ] denialReason
- [ ] appealText
- [ ] appealNumber

### Gateway Conditions
- [ ] Gateway_Eligible condition: `${isEligible}`
- [ ] Gateway_Auth_Status condition: `${authStatus}`

### Timer Configuration
- [ ] Timer definition: `PT48H`
- [ ] Timer attached to correct task
- [ ] Timeout flow defined

### Visual Elements
- [ ] Complete BPMNDiagram
- [ ] 4 lanes properly sized
- [ ] All elements positioned
- [ ] Timer boundary event properly attached

### Camunda Test
- [ ] Renders correctly
- [ ] Connector visible in properties
- [ ] Timer configuration valid
- [ ] Deploys successfully

---

## SUB_03: Admission and Registration

### File Existence
- [ ] File exists at `/src/bpmn/SUB_03_Admission.bpmn`
- [ ] Valid XML structure
- [ ] File size > 9KB

### Process Configuration
- [ ] Process ID: `Process_SUB_03_Admission`
- [ ] `isExecutable="true"`
- [ ] `camunda:historyTimeToLive` defined

### Lanes (3 Required)
- [ ] Lane_Self_Service
- [ ] Lane_Reception
- [ ] Lane_TASY_ADT

### Elements Count
- [ ] 1 Start Event
- [ ] 11 Service Tasks (minimum)
- [ ] 2 User Tasks
- [ ] 1 Exclusive Gateway
- [ ] 1 Embedded Event Subprocess (Emergency)
- [ ] 1 End Event

### Critical Service Tasks
- [ ] Task_Biometric_Auth (API: Biometria)
- [ ] Task_Document_OCR (RPA: OCR)
- [ ] Task_CPF_Validation (API: Receita Federal)
- [ ] Task_Credit_Check (API: Serasa/SPC)
- [ ] Task_Cost_Estimate (Rules Engine)
- [ ] Task_TASY_Admission (API: TASY ADT Create)
- [ ] Task_Generate_Bracelet (API: QR/RFID)
- [ ] Task_Assign_Room (API: TASY Bed Management)
- [ ] Task_Notify_Team (API: Push notification)

### Event Subprocess
- [ ] SubProcess_Emergency exists
- [ ] triggeredByEvent="true"
- [ ] Signal Start Event defined
- [ ] Signal reference: `Signal_Emergency`
- [ ] Fast track flow implemented

### Visual Elements
- [ ] Complete BPMNDiagram
- [ ] Event subprocess visually distinct
- [ ] Signal event properly shown

### Camunda Test
- [ ] Event subprocess renders
- [ ] Signal definition exists
- [ ] Deploys successfully

---

## SUB_04: Clinical Production and Documentation

### File Existence
- [ ] File exists at `/src/bpmn/SUB_04_Clinical_Production.bpmn`
- [ ] Valid XML structure
- [ ] File size > 11KB

### Process Configuration
- [ ] Process ID: `Process_SUB_04_Clinical_Production`
- [ ] `isExecutable="true"`
- [ ] `camunda:historyTimeToLive` defined

### Lanes (5 Required)
- [ ] Lane_Medical_Team
- [ ] Lane_Nursing
- [ ] Lane_Pharmacy
- [ ] Lane_IoT_RFID
- [ ] Lane_Integration

### Elements Count
- [ ] 1 Start Event
- [ ] 7 Service Tasks (minimum)
- [ ] 5 User Tasks
- [ ] 1 Intermediate Signal Event (Discharge)
- [ ] 1 End Event
- [ ] 1 Event Subprocess (Concurrent Audit)

### Critical Elements
- [ ] Task_Register_CID (with AI suggestion)
- [ ] Task_RFID_Capture (IoT integration)
- [ ] Task_Weight_Sensor (IoT integration)
- [ ] Task_LIS_Integration (Laboratory)
- [ ] Task_PACS_Integration (Imaging)
- [ ] Event_Signal_Discharge (Signal: Signal_Discharge)

### Event Subprocess - Concurrent Audit
- [ ] SubProcess_Concurrent_Audit exists
- [ ] triggeredByEvent="true"
- [ ] Timer Start Event with cycle: `R/PT1H`
- [ ] Task_Run_Audit_Rules
- [ ] Gateway_Audit_Issues
- [ ] Task_Create_Alert
- [ ] End Event

### Signal Definition
- [ ] Signal_Discharge defined in definitions
- [ ] Signal properly referenced

### Visual Elements
- [ ] 5 lanes properly laid out
- [ ] Event subprocess positioned correctly
- [ ] Timer cycle visible

### Camunda Test
- [ ] Renders with 5 lanes
- [ ] Audit subprocess shows timer
- [ ] Signal event configured
- [ ] Deploys successfully

---

## SUB_05: Coding and Internal Audit

### File Existence
- [ ] File exists at `/src/bpmn/SUB_05_Coding_Audit.bpmn`
- [ ] Valid XML structure
- [ ] File size > 9KB

### Process Configuration
- [ ] Process ID: `Process_SUB_05_Coding_Audit`
- [ ] `isExecutable="true"`
- [ ] `camunda:historyTimeToLive` defined

### Lanes (4 Required)
- [ ] Lane_AI_Coding
- [ ] Lane_Human_Coding
- [ ] Lane_Audit
- [ ] Lane_Quality

### Elements Count
- [ ] 1 Start Event
- [ ] 9 Service Tasks (minimum)
- [ ] 1 User Task
- [ ] 3 Exclusive Gateways
- [ ] 1 End Event

### Critical Service Tasks
- [ ] Task_AI_TUSS_Suggestion (LLM)
- [ ] Task_AI_DRG_Coding (DRG Engine)
- [ ] Task_Validate_CID_Proc (Rules)
- [ ] Task_Completeness_Check (Rules)
- [ ] Task_Request_Docs (Notification)
- [ ] Task_Internal_Audit (Rules)
- [ ] Task_Apply_Corrections (TASY API)
- [ ] Task_Quality_Score (Rules)

### Gateway Conditions
- [ ] Gateway_AI_Confidence: `${aiConfidence > 0.95}`
- [ ] Gateway_Complete: `${isComplete}`
- [ ] Gateway_Audit_Pass: `${auditPassed}`

### Variables (7 Required)
- [ ] accountId
- [ ] suggestedTUSSCodes
- [ ] suggestedDRG
- [ ] aiConfidence
- [ ] cidCodes
- [ ] isComplete
- [ ] missingDocs
- [ ] auditPassed
- [ ] auditFindings
- [ ] qualityScore

### Visual Elements
- [ ] 4 lanes organized
- [ ] Gateway conditions visible
- [ ] Flow clear AI→Human path

### Camunda Test
- [ ] Gateways evaluate correctly
- [ ] Variables pass through
- [ ] Deploys successfully

---

## SUB_06: Billing and Submission

### File Existence
- [ ] File exists at `/src/bpmn/SUB_06_Billing_Submission.bpmn`
- [ ] Valid XML structure
- [ ] File size > 10KB

### Process Configuration
- [ ] Process ID: `Process_SUB_06_Billing_Submission`
- [ ] `isExecutable="true"`
- [ ] `camunda:historyTimeToLive` defined

### Lanes (4 Required)
- [ ] Lane_Billing
- [ ] Lane_TISS_Engine
- [ ] Lane_Transmission
- [ ] Lane_Monitoring

### Elements Count
- [ ] 1 Start Event
- [ ] 10 Service Tasks (minimum)
- [ ] 1 User Task
- [ ] 2 Exclusive Gateways
- [ ] 1 Error Boundary Event
- [ ] 1 End Event

### Critical Service Tasks
- [ ] Task_Consolidate_Charges (TASY Billing)
- [ ] Task_Apply_Contract_Rules (Rules Engine)
- [ ] Task_Calculate_Values (Rules Engine)
- [ ] Task_Group_By_Guide (TASY)
- [ ] Task_Pre_Validation (TISS Rules)
- [ ] Task_Generate_TISS_Batch (TISS Generator)
- [ ] Task_Submit_Webservice (API)
- [ ] Task_Submit_Portal (RPA)
- [ ] Task_Capture_Protocol (API/RPA)
- [ ] Task_Retry_Submission (Retry: 3x with backoff)
- [ ] Task_Update_Status (TASY)

### Error Handling
- [ ] Error Boundary Event defined
- [ ] Error code: `Error_Transmission`
- [ ] Retry mechanism configured
- [ ] Backoff strategy defined

### Gateway Conditions
- [ ] Gateway_Valid: `${isValid}`
- [ ] Gateway_Submission_Type: `${submissionType}`

### Visual Elements
- [ ] Error boundary event attached correctly
- [ ] Retry path visible
- [ ] Clear validation loop

### Camunda Test
- [ ] Error event triggers correctly
- [ ] Retry configuration valid
- [ ] Deploys successfully

---

## SUB_07: Denials Management

### File Existence
- [ ] File exists at `/src/bpmn/SUB_07_Denials_Management.bpmn`
- [ ] Valid XML structure
- [ ] File size > 10KB

### Process Configuration
- [ ] Process ID: `Process_SUB_07_Denials_Management`
- [ ] `isExecutable="true"`
- [ ] `camunda:historyTimeToLive` defined

### Lanes (4 Required)
- [ ] Lane_Capture
- [ ] Lane_Analysis
- [ ] Lane_LLM_Appeals
- [ ] Lane_Negotiation

### Elements Count
- [ ] 1 Message Start Event
- [ ] 10 Service Tasks (minimum)
- [ ] 1 User Task
- [ ] 2 Exclusive Gateways
- [ ] 1 Timer Boundary Event (ANS deadline)
- [ ] 1 End Event

### Critical Service Tasks
- [ ] Task_RPA_Capture_Denials (RPA scraping)
- [ ] Task_Classify_Denial (AI)
- [ ] Task_Auto_Correct (Rules)
- [ ] Task_LLM_Analysis (LLM API)
- [ ] Task_Search_Evidence (TASY Docs)
- [ ] Task_Generate_Appeal (LLM)
- [ ] Task_Submit_Appeal (RPA)
- [ ] Task_Track_Response (RPA)
- [ ] Task_Register_Recovery (TASY Financial)
- [ ] Task_Register_Loss (TASY Financial)

### Timer Configuration
- [ ] Timer Boundary Event on tracking
- [ ] Calculated based on ANS deadline
- [ ] Escalation flow defined

### Gateway Conditions
- [ ] Gateway_Denial_Type: `${denialType}`
- [ ] Gateway_Appeal_Result: `${appealResult}`

### Visual Elements
- [ ] LLM lane clearly marked
- [ ] Timer boundary event visible
- [ ] Appeal flow highlighted

### Camunda Test
- [ ] Message start event configured
- [ ] Timer calculates correctly
- [ ] Deploys successfully

---

## SUB_08: Revenue Collection

### File Existence
- [ ] File exists at `/src/bpmn/SUB_08_Revenue_Collection.bpmn`
- [ ] Valid XML structure
- [ ] File size > 11KB

### Process Configuration
- [ ] Process ID: `Process_SUB_08_Revenue_Collection`
- [ ] `isExecutable="true"`
- [ ] `camunda:historyTimeToLive` defined

### Lanes (4 Required)
- [ ] Lane_Bank_Integration
- [ ] Lane_Reconciliation
- [ ] Lane_AR_Management
- [ ] Lane_Collection

### Elements Count
- [ ] 1 Timer Start Event (daily at 6am)
- [ ] 12 Service Tasks (minimum)
- [ ] 1 User Task
- [ ] 2 Exclusive Gateways
- [ ] 1 End Event

### Timer Configuration
- [ ] Timer definition: `0 6 * * *` (cron)
- [ ] Daily execution at 6am

### Critical Service Tasks
- [ ] Task_Process_CNAB (RPA Parser)
- [ ] Task_Process_PIX (API)
- [ ] Task_Auto_Matching (Rules)
- [ ] Task_Allocate_Payment (TASY Financial)
- [ ] Task_Analyze_Difference (Rules)
- [ ] Task_Create_Provision (TASY Accounting)
- [ ] Task_Aging_Analysis (Rules)
- [ ] Task_Collection_Workflow (RPA multicanal)
- [ ] Task_Negativation (Credit Bureau API)
- [ ] Task_Legal_Referral (Legal System API)
- [ ] Task_Write_Off (TASY Accounting)

### Gateway Conditions
- [ ] Gateway_Match_Found: `${matchFound}`
- [ ] Gateway_Difference_Type: `${differenceType}`

### Visual Elements
- [ ] Timer start event visible
- [ ] Cron expression shown
- [ ] Complex matching flow clear

### Camunda Test
- [ ] Timer triggers daily
- [ ] Cron expression valid
- [ ] Deploys successfully

---

## SUB_09: Analytics and BI

### File Existence
- [ ] File exists at `/src/bpmn/SUB_09_Analytics.bpmn`
- [ ] Valid XML structure
- [ ] File size > 10KB

### Process Configuration
- [ ] Process ID: `Process_SUB_09_Analytics`
- [ ] `isExecutable="true"`
- [ ] `camunda:historyTimeToLive` defined

### Lanes (4 Required)
- [ ] Lane_Data_Collection
- [ ] Lane_Processing
- [ ] Lane_KPI_Engine
- [ ] Lane_Reporting

### Elements Count
- [ ] 1 Timer Start Event (every 5 minutes)
- [ ] 13 Service Tasks (minimum)
- [ ] 1 Exclusive Gateway
- [ ] 1 End Event

### Timer Configuration
- [ ] Timer definition: `*/5 * * * *` (every 5 min)
- [ ] Cycle configured correctly

### Critical Service Tasks
- [ ] Task_Collect_TASY (Data Export API)
- [ ] Task_Collect_RPA_Logs (RPA Platform API)
- [ ] Task_Collect_External (External feeds)
- [ ] Task_Data_Quality (Rules)
- [ ] Task_Stream_Processing (Kafka/Spark)
- [ ] Task_Batch_Processing (Spark/ETL)
- [ ] Task_Data_Lake_Update (Data Lake API)
- [ ] Task_Calculate_KPIs (KPI Engine)
- [ ] Task_ML_Anomaly (ML: Isolation Forest)
- [ ] Task_ML_Prediction (ML: Time Series)
- [ ] Task_Create_Alert (Notification)
- [ ] Task_Update_Dashboard (Power BI)
- [ ] Task_Generate_Reports (RPA)

### Gateway Condition
- [ ] Gateway_Anomaly_Detected: `${anomalyDetected}`

### Visual Elements
- [ ] Timer cycle visible
- [ ] ML tasks marked
- [ ] Data flow clear

### Camunda Test
- [ ] Timer triggers every 5 min
- [ ] Async for performance
- [ ] Deploys successfully

---

## SUB_10: Revenue Maximization

### File Existence
- [ ] File exists at `/src/bpmn/SUB_10_Maximization.bpmn`
- [ ] Valid XML structure
- [ ] File size > 10KB

### Process Configuration
- [ ] Process ID: `Process_SUB_10_Maximization`
- [ ] `isExecutable="true"`
- [ ] `camunda:historyTimeToLive` defined

### Lanes (4 Required)
- [ ] Lane_Opportunity_Analysis
- [ ] Lane_VBHC
- [ ] Lane_Process_Mining
- [ ] Lane_Continuous_Improvement

### Elements Count
- [ ] 1 Timer Start Event (weekly)
- [ ] 13 Service Tasks (minimum)
- [ ] 1 User Task
- [ ] 1 End Event

### Timer Configuration
- [ ] Timer definition: weekly cycle
- [ ] Configured for strategic analysis

### Critical Service Tasks
- [ ] Task_Identify_Upsell (ML)
- [ ] Task_Analyze_Undercoding (ML)
- [ ] Task_Detect_Missed_Charges (Rules)
- [ ] Task_Benchmark_Analysis (External Data)
- [ ] Task_Cost_Analysis (TASY Cost)
- [ ] Task_Pricing_Simulation (Rules)
- [ ] Task_Bundle_Creation (Rules)
- [ ] Task_Margin_Monitoring (KPIs)
- [ ] Task_Process_Mining (Celonis/ProM)
- [ ] Task_Identify_Bottlenecks (ML)
- [ ] Task_Generate_Improvements (LLM)
- [ ] Task_Prioritize_Actions (Rules)
- [ ] Task_Track_Implementation (Project Mgmt)

### Visual Elements
- [ ] VBHC lane highlighted
- [ ] Process mining section clear
- [ ] ML tasks marked

### Camunda Test
- [ ] Weekly timer correct
- [ ] Complex analytics flow
- [ ] Deploys successfully

---

## CROSS-CUTTING VALIDATIONS

### ID Uniqueness
- [ ] No duplicate element IDs across all files
- [ ] All IDs follow naming conventions
- [ ] Process IDs unique across all subprocesses

### Variable Consistency
- [ ] Variables passed from orchestrator to subprocesses
- [ ] Return variables properly mapped
- [ ] No variable name conflicts

### Integration Points
- [ ] All called processes exist
- [ ] Signal definitions consistent
- [ ] Error definitions consistent
- [ ] Message definitions consistent

### Performance Configuration
- [ ] All service tasks have asyncBefore=true
- [ ] Critical tasks have jobPriority
- [ ] Timer jobs properly configured
- [ ] History time to live reasonable

### Security
- [ ] No hardcoded credentials
- [ ] API keys referenced as variables
- [ ] Sensitive data handling compliant

---

## DEPLOYMENT CHECKLIST

### Pre-Deployment
- [ ] All 11 BPMN files validated individually
- [ ] All cross-references validated
- [ ] All visual elements render correctly
- [ ] Camunda Modeler validation passed

### Deployment
- [ ] Deploy orchestrator first
- [ ] Deploy all subprocesses
- [ ] Verify all process definitions in Cockpit
- [ ] Check for deployment errors/warnings

### Post-Deployment
- [ ] Start test instance of orchestrator
- [ ] Verify call activities invoke subprocesses
- [ ] Check variable passing
- [ ] Verify error handling
- [ ] Test boundary events

### Production Readiness
- [ ] All delegates implemented
- [ ] All DMN tables deployed
- [ ] All external systems configured
- [ ] Monitoring configured
- [ ] Alerts configured

---

**Validation Status:** NOT STARTED - NO FILES EXIST
**Last Updated:** 2025-12-08
**Next Review:** After first BPMN files created
