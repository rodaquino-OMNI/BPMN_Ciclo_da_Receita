# BPMN Naming Conventions - Hospital Revenue Cycle

## Purpose

This document establishes mandatory naming conventions for all BPMN elements across the 11 processes (1 orchestrator + 10 subprocesses) to ensure consistency, maintainability, and Camunda 7 compatibility.

**Version**: 1.0.0
**Last Updated**: 2025-12-08
**Status**: MANDATORY

---

## 1. GENERAL PRINCIPLES

### 1.1 Core Rules

1. **Language**: Element IDs in English, display names in Portuguese (Brazil)
2. **Case Sensitivity**: IDs are case-sensitive
3. **Special Characters**: Use only alphanumeric, underscore `_`, and hyphen `-` in IDs
4. **No Spaces**: IDs must not contain spaces
5. **Uniqueness**: All IDs must be globally unique across all 11 files
6. **Descriptiveness**: Names should clearly indicate purpose and action

### 1.2 Naming Philosophy

```
[ElementType]_[Context]_[Action]_[Object]
```

Example: `Task_TASY_Validate_Eligibility`

---

## 2. PROCESS-LEVEL CONVENTIONS

### 2.1 Process IDs

**Pattern**: `Process_[Type]_[Name]`

| Type | Description | Example |
|------|-------------|---------|
| ORCH | Orchestrator | `Process_ORCH_Revenue_Cycle` |
| SUB_XX | Subprocess (01-10) | `Process_SUB_01_First_Contact` |

**Rules**:
- Orchestrator always uses `ORCH` prefix
- Subprocesses use `SUB_XX` where XX is 01-10 (zero-padded)
- Name part uses underscores, no hyphens

**Examples**:
```xml
<bpmn:process id="Process_ORCH_Revenue_Cycle" name="Orquestrador Ciclo de Receita" isExecutable="true">

<bpmn:process id="Process_SUB_01_First_Contact" name="Primeiro Contato e Agendamento" isExecutable="true">

<bpmn:process id="Process_SUB_07_Denials_Management" name="Gestão de Glosas e Recursos" isExecutable="true">
```

### 2.2 Collaboration IDs

**Pattern**: `Collaboration_[ProcessName]`

```xml
<bpmn:collaboration id="Collaboration_Revenue_Cycle_Orchestrator">
<bpmn:collaboration id="Collaboration_First_Contact">
```

### 2.3 File Names

**Pattern**: `[TYPE]_[Number]_[Name].bpmn`

| File | Name |
|------|------|
| Orchestrator | `ORCH_Ciclo_Receita_Hospital_Futuro.bpmn` |
| Subprocess 01 | `SUB_01_First_Contact.bpmn` |
| Subprocess 02 | `SUB_02_Pre_Authorization.bpmn` |
| ... | ... |
| Subprocess 10 | `SUB_10_Maximization.bpmn` |

---

## 3. PARTICIPANT (POOL) CONVENTIONS

### 3.1 Participant IDs

**Pattern**: `Participant_[Name]`

```xml
<!-- Internal Pool (White Box) -->
<bpmn:participant id="Participant_Hospital" name="Hospital - Ciclo de Receita" processRef="Process_ORCH_Revenue_Cycle" />

<!-- External Pools (Black Box) -->
<bpmn:participant id="Participant_Patient" name="Paciente / Responsável" />
<bpmn:participant id="Participant_Insurance" name="Operadora de Saúde" />
<bpmn:participant id="Participant_TASY" name="Sistema TASY ERP" />
<bpmn:participant id="Participant_Government" name="Órgãos Reguladores (ANS/RF)" />
<bpmn:participant id="Participant_Bank" name="Instituições Financeiras" />
```

**Rules**:
- Use singular form
- Use generic role names, not specific organizations
- External participants must NOT have `processRef` attribute

### 3.2 Reserved Participant Names

| ID | Purpose | Display Name |
|----|---------|--------------|
| `Participant_Hospital` | Internal process owner | Hospital - Ciclo de Receita |
| `Participant_Patient` | Patient/family | Paciente / Responsável |
| `Participant_Insurance` | Health insurance | Operadora de Saúde |
| `Participant_TASY` | ERP system | Sistema TASY ERP |
| `Participant_Government` | Regulatory bodies | Órgãos Reguladores (ANS/RF) |
| `Participant_Bank` | Financial institutions | Instituições Financeiras |

---

## 4. LANE CONVENTIONS

### 4.1 Lane IDs (Orchestrator)

**Pattern**: `Lane_[Number]_[Name]`

```xml
<bpmn:lane id="Lane_01_First_Contact" name="1. Primeiro Contato / Agendamento">
<bpmn:lane id="Lane_02_Pre_Authorization" name="2. Pré-Autorização / Elegibilidade">
...
<bpmn:lane id="Lane_10_Maximization" name="10. Maximização de Receita">
```

**Rules**:
- Number from 01-10 (zero-padded)
- Number must match subprocess number
- Display name includes ordinal number + Portuguese description

### 4.2 Lane IDs (Subprocesses)

**Pattern**: `Lane_[Role]_[Function]`

**Subprocess 01 Example**:
```xml
<bpmn:lane id="Lane_Digital_Channels" name="Canais Digitais (WhatsApp/Portal/App)">
<bpmn:lane id="Lane_Call_Center" name="Central de Atendimento">
<bpmn:lane id="Lane_TASY_Scheduling" name="Sistema TASY Agendamento">
```

**Subprocess 02 Example**:
```xml
<bpmn:lane id="Lane_Eligibility" name="Verificação de Elegibilidade">
<bpmn:lane id="Lane_Authorization" name="Autorização">
<bpmn:lane id="Lane_RPA_Portals" name="RPA Portais Operadoras">
<bpmn:lane id="Lane_Appeals" name="Recursos e Negativas">
```

**Rules**:
- Use descriptive role/function names
- No numbers in subprocess lane IDs
- Avoid abbreviations unless universally understood

---

## 5. TASK CONVENTIONS

### 5.1 Service Task IDs

**Pattern**: `Task_[System]_[Action]_[Object]`

```xml
<bpmn:serviceTask id="Task_TASY_Create_Patient" name="Criar Cadastro TASY" />
<bpmn:serviceTask id="Task_RPA_Check_Eligibility" name="Verificar Elegibilidade (RPA)" />
<bpmn:serviceTask id="Task_LLM_Generate_Appeal" name="Gerar Recurso com IA" />
```

**System Prefixes**:
- `TASY` - TASY ERP operations
- `RPA` - Robotic Process Automation
- `LLM` - Large Language Model / AI
- `API` - Generic API call
- `IoT` - IoT device interaction
- `DB` - Direct database operation

### 5.2 User Task IDs

**Pattern**: `Task_[Role]_[Action]`

```xml
<bpmn:userTask id="Task_Agent_Capture_Data" name="Capturar Dados do Paciente" />
<bpmn:userTask id="Task_Coder_Review_Codes" name="Revisar Codificação" />
<bpmn:userTask id="Task_Auditor_Approve_Claim" name="Aprovar Conta" />
```

### 5.3 Manual Task IDs

**Pattern**: `Task_Manual_[Action]`

```xml
<bpmn:manualTask id="Task_Manual_Verify_Documents" name="Verificar Documentos Físicos" />
```

### 5.4 Script Task IDs

**Pattern**: `Task_Script_[Purpose]`

```xml
<bpmn:scriptTask id="Task_Script_Calculate_Total" name="Calcular Total" />
```

### 5.5 Business Rule Task IDs

**Pattern**: `Task_Rule_[Decision]`

```xml
<bpmn:businessRuleTask id="Task_Rule_Determine_Authorization" name="Aplicar Regra de Autorização" />
```

### 5.6 Send/Receive Task IDs

**Pattern**: `Task_Send_[Message]` or `Task_Receive_[Message]`

```xml
<bpmn:sendTask id="Task_Send_Confirmation" name="Enviar Confirmação" />
<bpmn:receiveTask id="Task_Receive_Payment" name="Aguardar Pagamento" />
```

---

## 6. EVENT CONVENTIONS

### 6.1 Start Event IDs

**Pattern**: `Event_Start_[Trigger]`

```xml
<bpmn:startEvent id="Event_Start_Patient_Contact" name="Contato Paciente">
<bpmn:startEvent id="Event_Start_Timer" name="Início Ciclo">
  <bpmn:timerEventDefinition />
</bpmn:startEvent>
<bpmn:startEvent id="Event_Start_Message" name="Mensagem Recebida">
  <bpmn:messageEventDefinition />
</bpmn:startEvent>
```

**Types**:
- `Event_Start_[Name]` - None start event
- `Event_Start_Timer_[Name]` - Timer start event
- `Event_Start_Message_[Name]` - Message start event
- `Event_Start_Signal_[Name]` - Signal start event

### 6.2 End Event IDs

**Pattern**: `Event_End_[Outcome]`

```xml
<bpmn:endEvent id="Event_End_Completed" name="Processo Concluído">
<bpmn:endEvent id="Event_End_Error_Timeout" name="Erro: Timeout">
  <bpmn:errorEventDefinition />
</bpmn:endEvent>
<bpmn:endEvent id="Event_End_Terminate" name="Processo Cancelado">
  <bpmn:terminateEventDefinition />
</bpmn:endEvent>
```

**Types**:
- `Event_End_[Name]` - None end event
- `Event_End_Error_[Type]` - Error end event
- `Event_End_Terminate` - Terminate end event
- `Event_End_Message_[Name]` - Message end event

### 6.3 Intermediate Events

**Pattern**: `Event_[Type]_[Name]`

```xml
<!-- Catching Events -->
<bpmn:intermediateCatchEvent id="Event_Timer_Reminder" name="Aguardar 24h">
  <bpmn:timerEventDefinition />
</bpmn:intermediateCatchEvent>

<bpmn:intermediateCatchEvent id="Event_Message_Authorization" name="Aguardar Autorização">
  <bpmn:messageEventDefinition />
</bpmn:intermediateCatchEvent>

<!-- Throwing Events -->
<bpmn:intermediateThrowEvent id="Event_Signal_Discharge" name="Sinalizar Alta">
  <bpmn:signalEventDefinition />
</bpmn:intermediateThrowEvent>
```

### 6.4 Boundary Events

**Pattern**: `Event_Boundary_[Type]_[Purpose]`

```xml
<bpmn:boundaryEvent id="Event_Boundary_Timer_Timeout" name="Timeout 48h" attachedToRef="Task_Wait_Authorization">
  <bpmn:timerEventDefinition>
    <bpmn:timeDuration>PT48H</bpmn:timeDuration>
  </bpmn:timerEventDefinition>
</bpmn:boundaryEvent>

<bpmn:boundaryEvent id="Event_Boundary_Error_Transmission" name="Erro Transmissão" attachedToRef="Task_Submit_Claim">
  <bpmn:errorEventDefinition errorRef="Error_Transmission_Failed" />
</bpmn:boundaryEvent>
```

**Types**:
- `Event_Boundary_Timer_[Purpose]` - Timer boundary
- `Event_Boundary_Error_[Type]` - Error boundary
- `Event_Boundary_Signal_[Name]` - Signal boundary
- `Event_Boundary_Escalation_[Name]` - Escalation boundary
- `Event_Boundary_Compensation_[Name]` - Compensation boundary

---

## 7. GATEWAY CONVENTIONS

### 7.1 Exclusive Gateway IDs

**Pattern**: `Gateway_[Decision]`

```xml
<bpmn:exclusiveGateway id="Gateway_Has_Insurance" name="Tem Convênio?">
<bpmn:exclusiveGateway id="Gateway_Authorization_Status" name="Status Autorização?">
<bpmn:exclusiveGateway id="Gateway_Document_Complete" name="Documentação Completa?">
```

**Rules**:
- Use question format for display name
- ID describes the decision point
- Should be a clear yes/no or multi-choice question

### 7.2 Parallel Gateway IDs

**Pattern**: `Gateway_Parallel_[Purpose]_[Split/Join]`

```xml
<bpmn:parallelGateway id="Gateway_Parallel_Analytics_Split" name="Análise Paralela">
<bpmn:parallelGateway id="Gateway_Parallel_Analytics_Join" name="Aguardar Conclusão">
```

**Rules**:
- Always use paired split/join gateways
- Split indicates fork, Join indicates merge
- Purpose describes what's being parallelized

### 7.3 Inclusive Gateway IDs

**Pattern**: `Gateway_Inclusive_[Purpose]_[Split/Join]`

```xml
<bpmn:inclusiveGateway id="Gateway_Inclusive_Notifications_Split" name="Enviar Notificações">
<bpmn:inclusiveGateway id="Gateway_Inclusive_Notifications_Join" name="Aguardar Envios">
```

### 7.4 Event-Based Gateway IDs

**Pattern**: `Gateway_Event_[Situation]`

```xml
<bpmn:eventBasedGateway id="Gateway_Event_Wait_Response" name="Aguardar Resposta ou Timeout">
```

---

## 8. SUBPROCESS CONVENTIONS

### 8.1 Call Activity IDs

**Pattern**: `CallActivity_[SubprocessName]`

```xml
<bpmn:callActivity id="CallActivity_SUB_01" name="Primeiro Contato" calledElement="Process_SUB_01_First_Contact">
<bpmn:callActivity id="CallActivity_SUB_07" name="Gestão de Glosas" calledElement="Process_SUB_07_Denials_Management">
```

**Rules**:
- ID must reference subprocess number (SUB_XX)
- `calledElement` must match exact process ID
- Display name is short description

### 8.2 Embedded Subprocess IDs

**Pattern**: `SubProcess_[Purpose]`

```xml
<bpmn:subProcess id="SubProcess_Emergency_Admission" name="Admissão Emergência">
<bpmn:subProcess id="SubProcess_Concurrent_Audit" name="Auditoria Concorrente" triggeredByEvent="true">
```

**Types**:
- Regular embedded: `SubProcess_[Name]`
- Event subprocess: `SubProcess_Event_[Name]`

---

## 9. SEQUENCE FLOW CONVENTIONS

### 9.1 Sequence Flow IDs

**Pattern**: `Flow_[SourceId]_to_[TargetId]`

```xml
<bpmn:sequenceFlow id="Flow_Start_to_Task1" sourceRef="Event_Start_Process" targetRef="Task_Capture_Data" />
<bpmn:sequenceFlow id="Flow_Gateway1_to_Task2" sourceRef="Gateway_Has_Insurance" targetRef="Task_Check_Eligibility">
  <bpmn:conditionExpression xsi:type="bpmn:tFormalExpression">${hasInsurance == true}</bpmn:conditionExpression>
</bpmn:sequenceFlow>
```

**Rules**:
- Always use source and target element IDs
- Use `to` connector (not underscore)
- Abbreviated IDs acceptable if length exceeds 60 chars

### 9.2 Conditional Flow Naming

```xml
<bpmn:sequenceFlow id="Flow_Gateway1_Yes" name="Sim" sourceRef="Gateway_Has_Insurance" targetRef="Task_Pre_Auth">
  <bpmn:conditionExpression xsi:type="bpmn:tFormalExpression">${hasInsurance}</bpmn:conditionExpression>
</bpmn:sequenceFlow>

<bpmn:sequenceFlow id="Flow_Gateway1_No" name="Não" sourceRef="Gateway_Has_Insurance" targetRef="Task_Register_Private">
  <bpmn:conditionExpression xsi:type="bpmn:tFormalExpression">${!hasInsurance}</bpmn:conditionExpression>
</bpmn:sequenceFlow>
```

**Naming for Branches**:
- Yes/No: `Sim` / `Não`
- Approved/Denied: `Aprovado` / `Negado`
- Complete/Incomplete: `Completo` / `Incompleto`

---

## 10. MESSAGE FLOW CONVENTIONS

### 10.1 Message Flow IDs

**Pattern**: `MsgFlow_[SourceParticipant]_[TargetParticipant]_[Message]`

```xml
<bpmn:messageFlow id="MsgFlow_Patient_Hospital_Request"
  name="Solicitação"
  sourceRef="Participant_Patient"
  targetRef="Event_Start_Patient_Contact" />

<bpmn:messageFlow id="MsgFlow_Hospital_TASY_CreatePatient"
  name="Criar Paciente"
  sourceRef="Task_TASY_Create_Patient"
  targetRef="Participant_TASY" />
```

**Rules**:
- Source and target can be participants or specific elements
- Name describes the message content
- Use descriptive message names

---

## 11. DATA OBJECT CONVENTIONS

### 11.1 Data Object IDs

**Pattern**: `DataObject_[Name]`

```xml
<bpmn:dataObjectReference id="DataObject_Patient_Record" name="Prontuário Paciente" dataObjectRef="DataObject_Patient" />
<bpmn:dataObject id="DataObject_Patient" />
```

### 11.2 Data Store IDs

**Pattern**: `DataStore_[System]_[Entity]`

```xml
<bpmn:dataStoreReference id="DataStore_TASY_Patients" name="Base Pacientes TASY" />
<bpmn:dataStoreReference id="DataStore_DWH_Analytics" name="Data Warehouse Analytics" />
```

---

## 12. DEFINITIONS & REFERENCES

### 12.1 Error Definitions

**Pattern**: `Error_[Category]_[Type]`

```xml
<bpmn:error id="Error_Transmission_Failed" name="Falha de Transmissão" errorCode="ERR_TRANSMISSION_001" />
<bpmn:error id="Error_Authorization_Timeout" name="Timeout Autorização" errorCode="ERR_AUTH_002" />
<bpmn:error id="Error_System_Unavailable" name="Sistema Indisponível" errorCode="ERR_SYS_003" />
```

**Error Code Format**: `ERR_[CATEGORY]_[NUMBER]`

### 12.2 Message Definitions

**Pattern**: `Message_[Purpose]`

```xml
<bpmn:message id="Message_Authorization_Request" name="Solicitação de Autorização" />
<bpmn:message id="Message_Denial_Notification" name="Notificação de Glosa" />
<bpmn:message id="Message_Payment_Received" name="Pagamento Recebido" />
```

### 12.3 Signal Definitions

**Pattern**: `Signal_[Event]`

```xml
<bpmn:signal id="Signal_Patient_Discharge" name="Alta do Paciente" />
<bpmn:signal id="Signal_Emergency_Admission" name="Admissão Emergência" />
<bpmn:signal id="Signal_Critical_Alert" name="Alerta Crítico" />
```

### 12.4 Escalation Definitions

**Pattern**: `Escalation_[Situation]`

```xml
<bpmn:escalation id="Escalation_ANS_Deadline" name="Prazo ANS Crítico" escalationCode="ESC_ANS_001" />
<bpmn:escalation id="Escalation_Management_Review" name="Revisão Gerencial" escalationCode="ESC_MGT_002" />
```

---

## 13. DIAGRAM ELEMENT IDs

### 13.1 BPMNDiagram IDs

**Pattern**: `BPMNDiagram_[ProcessId]`

```xml
<bpmndi:BPMNDiagram id="BPMNDiagram_Process_ORCH_Revenue_Cycle">
  <bpmndi:BPMNPlane id="BPMNPlane_Process_ORCH_Revenue_Cycle" bpmnElement="Collaboration_Revenue_Cycle_Orchestrator">
```

### 13.2 BPMNShape IDs

**Pattern**: `[ElementId]_di`

```xml
<bpmndi:BPMNShape id="Participant_Hospital_di" bpmnElement="Participant_Hospital" isHorizontal="true">
<bpmndi:BPMNShape id="Lane_01_First_Contact_di" bpmnElement="Lane_01_First_Contact" isHorizontal="true">
<bpmndi:BPMNShape id="Task_TASY_Create_Patient_di" bpmnElement="Task_TASY_Create_Patient">
```

**Rule**: Append `_di` (diagram information) to the element ID

### 13.3 BPMNEdge IDs

**Pattern**: `[FlowId]_di`

```xml
<bpmndi:BPMNEdge id="Flow_Start_to_Task1_di" bpmnElement="Flow_Start_to_Task1">
<bpmndi:BPMNEdge id="MsgFlow_Patient_Hospital_Request_di" bpmnElement="MsgFlow_Patient_Hospital_Request">
```

---

## 14. CAMUNDA-SPECIFIC CONVENTIONS

### 14.1 Connector IDs

**Pattern**: `[system]-[operation]-connector`

```xml
<camunda:connectorId>tasy-api-connector</camunda:connectorId>
<camunda:connectorId>ibm-rpa-eligibility-bot</camunda:connectorId>
<camunda:connectorId>llm-api-connector</camunda:connectorId>
```

### 14.2 Form Keys

**Pattern**: `embedded:app:forms/[process]/[form-name].html`

```xml
<camunda:formKey>embedded:app:forms/first-contact/capture-patient-data.html</camunda:formKey>
<camunda:formKey>embedded:app:forms/pre-auth/review-authorization.html</camunda:formKey>
```

### 14.3 External Task Topics

**Pattern**: `[system]-[operation]`

```xml
<camunda:topic>tasy-create-patient</camunda:topic>
<camunda:topic>rpa-check-eligibility</camunda:topic>
<camunda:topic>llm-generate-appeal</camunda:topic>
```

---

## 15. VALIDATION CHECKLIST

Use this checklist when creating or reviewing BPMN files:

- [ ] All IDs follow established patterns
- [ ] No ID duplicates across files
- [ ] All IDs use only allowed characters (alphanumeric, `_`, `-`)
- [ ] All display names are in Portuguese
- [ ] All element IDs are in English
- [ ] Sequence flows have descriptive IDs
- [ ] Gateway questions use question format
- [ ] All diagram elements have `_di` suffix
- [ ] Connector IDs use lowercase with hyphens
- [ ] Error codes follow `ERR_[CATEGORY]_[NUMBER]` format

---

## 16. EXAMPLES BY SUBPROCESS

### Example: SUB_01 (First Contact)

```xml
<!-- Process -->
<bpmn:process id="Process_SUB_01_First_Contact" name="Primeiro Contato e Agendamento" isExecutable="true">

  <!-- Lane -->
  <bpmn:lane id="Lane_Digital_Channels" name="Canais Digitais">

    <!-- Start Event -->
    <bpmn:startEvent id="Event_Start_Contact" name="Solicitação Recebida" />

    <!-- Service Task -->
    <bpmn:serviceTask id="Task_RPA_Identify_Channel" name="Identificar Canal">
      <bpmn:extensionElements>
        <camunda:connector>
          <camunda:connectorId>rpa-channel-classifier</camunda:connectorId>
        </camunda:connector>
      </bpmn:extensionElements>
    </bpmn:serviceTask>

    <!-- User Task -->
    <bpmn:userTask id="Task_Agent_Capture_Data" name="Capturar Dados Paciente">
      <bpmn:extensionElements>
        <camunda:formKey>embedded:app:forms/first-contact/patient-data.html</camunda:formKey>
      </bpmn:extensionElements>
    </bpmn:userTask>

    <!-- Gateway -->
    <bpmn:exclusiveGateway id="Gateway_Patient_Exists" name="Paciente Cadastrado?" />

    <!-- Sequence Flows -->
    <bpmn:sequenceFlow id="Flow_Start_to_Identify" sourceRef="Event_Start_Contact" targetRef="Task_RPA_Identify_Channel" />
    <bpmn:sequenceFlow id="Flow_Identify_to_Capture" sourceRef="Task_RPA_Identify_Channel" targetRef="Task_Agent_Capture_Data" />
    <bpmn:sequenceFlow id="Flow_Capture_to_Gateway" sourceRef="Task_Agent_Capture_Data" targetRef="Gateway_Patient_Exists" />

  </bpmn:lane>
</bpmn:process>
```

---

## APPENDIX A: QUICK REFERENCE

### Element Type Prefixes

| Element | Prefix | Example |
|---------|--------|---------|
| Process | `Process_` | `Process_SUB_01_First_Contact` |
| Participant | `Participant_` | `Participant_Hospital` |
| Lane | `Lane_` | `Lane_Digital_Channels` |
| Task | `Task_` | `Task_TASY_Create_Patient` |
| Gateway | `Gateway_` | `Gateway_Has_Insurance` |
| Event | `Event_` | `Event_Start_Contact` |
| Flow | `Flow_` | `Flow_Start_to_Task1` |
| Message Flow | `MsgFlow_` | `MsgFlow_Hospital_TASY` |
| Subprocess | `SubProcess_` | `SubProcess_Emergency` |
| Call Activity | `CallActivity_` | `CallActivity_SUB_01` |
| Data Object | `DataObject_` | `DataObject_Patient_Record` |
| Error | `Error_` | `Error_Transmission_Failed` |
| Signal | `Signal_` | `Signal_Patient_Discharge` |

---

**Document Status**: APPROVED
**Enforcement**: MANDATORY for all BPMN files
**Non-Compliance**: May result in deployment failures or maintenance issues
