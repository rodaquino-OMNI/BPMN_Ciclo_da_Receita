package com.hospital.e2e;

import com.hospital.fixtures.BillingFixtures;
import com.hospital.fixtures.ClinicalFixtures;
import com.hospital.fixtures.InsuranceFixtures;
import com.hospital.fixtures.PatientFixtures;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.extension.junit5.test.ProcessEngineExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
import static org.assertj.core.api.Assertions.*;

/**
 * End-to-end tests for the complete Revenue Cycle.
 * Tests integration of all subprocesses from patient registration to payment collection.
 */
@ExtendWith(ProcessEngineExtension.class)
@DisplayName("Revenue Cycle End-to-End Tests")
class RevenueCycleE2ETest {

    @Test
    @Deployment(resources = {
        "bpmn/ORCH_Ciclo_Receita_Hospital_Futuro.bpmn",
        "bpmn/SUB_01_Agendamento_Registro.bpmn",
        "bpmn/SUB_02_Pre_Atendimento.bpmn",
        "bpmn/SUB_03_Atendimento_Clinico.bpmn"
    })
    @DisplayName("Should complete full revenue cycle for insured patient consultation")
    void shouldCompleteFullRevenueCycleForInsuredPatientConsultation() {
        // Arrange - Patient Registration
        PatientFixtures.Patient patient = PatientFixtures.withInsurance("UNIMED");

        Map<String, Object> variables = new HashMap<>();
        variables.put("patientCPF", patient.getCpf());
        variables.put("patientName", patient.getName());
        variables.put("insuranceId", "UNIMED");
        variables.put("serviceType", "CONSULTATION");

        // Act - Start orchestrator process
        ProcessInstance orchestrator = runtimeService()
            .startProcessInstanceByKey("Process_ORCH_Ciclo_Receita", variables);

        assertThat(orchestrator).isStarted();

        // SUB_01: First Contact & Scheduling
        assertThat(orchestrator).isWaitingAt("CallActivity_SUB_01_FirstContact");

        complete(task(), withVariables(
            "appointmentId", "APPT123",
            "appointmentDateTime", LocalDateTime.now().plusDays(7),
            "appointmentStatus", "CONFIRMED"
        ));

        // SUB_02: Pre-Attendance
        assertThat(orchestrator).isWaitingAt("CallActivity_SUB_02_PreAttendance");

        // Insurance verification
        complete(task(), withVariables(
            "insuranceVerified", true,
            "insurancePlanCode", "UNIMED_PREMIUM_001",
            "eligible", true,
            "coveragePercent", 80,
            "coPayPercent", 20
        ));

        // SUB_03: Clinical Attendance
        assertThat(orchestrator).isWaitingAt("CallActivity_SUB_03_ClinicalAttendance");

        // Admission
        complete(task(), withVariables(
            "admissionId", "ADM123",
            "admissionType", "OUTPATIENT"
        ));

        // Clinical documentation
        complete(task(), withVariables(
            "cidCodes", Arrays.asList("Z00.0"),
            "procedureCodes", Arrays.asList("10101012"),
            "documentationComplete", true
        ));

        // Discharge
        complete(task(), withVariables(
            "dischargeType", "REGULAR",
            "dischargeDateTime", LocalDateTime.now(),
            "readyForBilling", true
        ));

        // SUB_04: Medical Coding (implied in discharge)
        complete(task(), withVariables(
            "codingValid", true,
            "codingComplete", true
        ));

        // SUB_05: Billing
        assertThat(orchestrator).isWaitingAt("CallActivity_SUB_05_Billing");

        complete(task(), withVariables(
            "totalAmount", new BigDecimal("300.00"),
            "coPayAmount", new BigDecimal("60.00"),
            "insuranceAmount", new BigDecimal("240.00"),
            "tissGuideNumber", "TISS123456",
            "protocolNumber", "PROT987654"
        ));

        // SUB_06: Submission & Collection
        assertThat(orchestrator).isWaitingAt("CallActivity_SUB_06_Submission");

        complete(task(), withVariables(
            "submissionStatus", "APPROVED",
            "paymentReceived", true,
            "paidAmount", new BigDecimal("240.00")
        ));

        // SUB_07: Patient Co-Pay Collection
        complete(task(), withVariables(
            "coPayCollected", true,
            "coPayMethod", "CREDIT_CARD"
        ));

        // Assert - Process completes successfully
        assertThat(orchestrator).isEnded();

        // Verify final state
        Map<String, Object> finalVars = historyService()
            .createHistoricVariableInstanceQuery()
            .processInstanceId(orchestrator.getId())
            .list()
            .stream()
            .collect(java.util.stream.Collectors.toMap(
                v -> v.getName(),
                v -> v.getValue()
            ));

        assertThat(finalVars)
            .containsEntry("paymentReceived", true)
            .containsEntry("coPayCollected", true);
    }

    @Test
    @Deployment(resources = {
        "bpmn/ORCH_Ciclo_Receita_Hospital_Futuro.bpmn",
        "bpmn/SUB_01_Agendamento_Registro.bpmn",
        "bpmn/SUB_02_Pre_Atendimento.bpmn",
        "bpmn/SUB_03_Atendimento_Clinico.bpmn"
    })
    @DisplayName("Should complete surgical procedure with authorization workflow")
    void shouldCompleteSurgicalProcedureWithAuthorizationWorkflow() {
        // Arrange
        PatientFixtures.Patient patient = PatientFixtures.withInsurance("UNIMED");
        ClinicalFixtures.ClinicalData surgery = ClinicalFixtures.surgicalProcedure();

        Map<String, Object> variables = new HashMap<>();
        variables.put("patientCPF", patient.getCpf());
        variables.put("serviceType", "SURGERY");
        variables.put("insuranceId", "UNIMED");
        variables.put("requiresAuthorization", true);

        // Act
        ProcessInstance orchestrator = runtimeService()
            .startProcessInstanceByKey("Process_ORCH_Ciclo_Receita", variables);

        // SUB_01: Scheduling
        complete(task(), withVariables(
            "appointmentId", "APPT_SURG_456",
            "appointmentStatus", "CONFIRMED"
        ));

        // SUB_02: Authorization Request
        assertThat(orchestrator).isWaitingAt("Task_RequestAuthorization");

        complete(task(), withVariables(
            "authorizationNumber", "AUTH123456",
            "authorizationMethod", "WEBSERVICE",
            "authorizationStatus", "PENDING"
        ));

        // Check authorization status (polling)
        for (int i = 0; i < 3; i++) {
            complete(task(), withVariables(
                "authorizationStatus", i < 2 ? "PENDING" : "APPROVED"
            ));
        }

        // SUB_03: Surgical procedure
        complete(task(), withVariables(
            "admissionId", "ADM_SURG_456",
            "admissionType", "INPATIENT",
            "procedureCodes", surgery.getProcedures().stream()
                .map(ClinicalFixtures.Procedure::getTusscode)
                .toList(),
            "cidCodes", surgery.getCidCodes()
        ));

        // Continue through billing
        complete(task(), withVariables(
            "totalAmount", new BigDecimal("12000.00"),
            "insuranceAmount", new BigDecimal("9600.00")
        ));

        // Assert - Authorization tracked throughout
        Map<String, Object> vars = runtimeService().getVariables(orchestrator.getId());
        assertThat(vars)
            .containsKey("authorizationNumber")
            .containsEntry("authorizationStatus", "APPROVED");
    }

    @Test
    @Deployment(resources = {
        "bpmn/ORCH_Ciclo_Receita_Hospital_Futuro.bpmn",
        "bpmn/SUB_01_Agendamento_Registro.bpmn",
        "bpmn/SUB_03_Atendimento_Clinico.bpmn"
    })
    @DisplayName("Should handle private pay patient without insurance")
    void shouldHandlePrivatePayPatientWithoutInsurance() {
        // Arrange
        PatientFixtures.Patient patient = PatientFixtures.withoutInsurance();

        Map<String, Object> variables = new HashMap<>();
        variables.put("patientCPF", patient.getCpf());
        variables.put("serviceType", "CONSULTATION");
        variables.put("paymentType", "PARTICULAR");

        // Act
        ProcessInstance orchestrator = runtimeService()
            .startProcessInstanceByKey("Process_ORCH_Ciclo_Receita", variables);

        // SUB_01: Scheduling
        complete(task(), withVariables(
            "appointmentId", "APPT789",
            "appointmentStatus", "CONFIRMED"
        ));

        // Skip SUB_02 (no insurance verification needed)
        assertThat(orchestrator).isNotWaitingAt("CallActivity_SUB_02_PreAttendance");

        // SUB_03: Clinical care
        complete(task(), withVariables(
            "admissionId", "ADM789",
            "dischargeType", "REGULAR"
        ));

        // Billing - full amount to patient
        complete(task(), withVariables(
            "totalAmount", new BigDecimal("300.00"),
            "patientAmount", new BigDecimal("300.00"),
            "insuranceAmount", BigDecimal.ZERO
        ));

        // Patient payment
        complete(task(), withVariables(
            "paymentMethod", "CASH",
            "paymentReceived", true,
            "paidAmount", new BigDecimal("300.00")
        ));

        // Assert
        assertThat(orchestrator).isEnded();
    }

    @Test
    @Deployment(resources = {
        "bpmn/ORCH_Ciclo_Receita_Hospital_Futuro.bpmn",
        "bpmn/SUB_03_Atendimento_Clinico.bpmn"
    })
    @DisplayName("Should handle emergency admission without prior scheduling")
    void shouldHandleEmergencyAdmissionWithoutPriorScheduling() {
        // Arrange
        PatientFixtures.Patient patient = PatientFixtures.emergencyPatient();
        ClinicalFixtures.ClinicalData emergency = ClinicalFixtures.emergencyAdmission();

        Map<String, Object> variables = new HashMap<>();
        variables.put("patientCPF", patient.getCpf());
        variables.put("admissionType", "EMERGENCY");
        variables.put("skipScheduling", true);

        // Act
        ProcessInstance orchestrator = runtimeService()
            .startProcessInstanceByKey("Process_ORCH_Ciclo_Receita", variables);

        // Skip SUB_01 (no scheduling for emergency)
        assertThat(orchestrator).isNotWaitingAt("CallActivity_SUB_01_FirstContact");

        // Direct to SUB_03: Emergency care
        assertThat(orchestrator).isWaitingAt("CallActivity_SUB_03_ClinicalAttendance");

        complete(task(), withVariables(
            "admissionId", emergency.getAccountId(),
            "admissionType", "EMERGENCY",
            "cidCodes", emergency.getCidCodes(),
            "requiresPostAuthorization", true
        ));

        // Post-authorization for emergency
        complete(task(), withVariables(
            "authorizationNumber", "AUTH_EMERG_999",
            "authorizationStatus", "POST_APPROVED"
        ));

        // Continue with billing
        assertThat(orchestrator).isActive();
    }

    @Test
    @Deployment(resources = {
        "bpmn/ORCH_Ciclo_Receita_Hospital_Futuro.bpmn",
        "bpmn/SUB_06_Cobranca_Glosa.bpmn"
    })
    @DisplayName("Should handle glosa and appeal workflow")
    void shouldHandleGlosaAndAppealWorkflow() {
        // Arrange
        BillingFixtures.BillingData billing = BillingFixtures.accountWithDenials();

        Map<String, Object> variables = new HashMap<>();
        variables.put("accountId", billing.getAccountId());
        variables.put("tissGuideNumber", "TISS123456");
        variables.put("submissionStatus", "DENIED");

        // Act
        ProcessInstance orchestrator = runtimeService()
            .startProcessInstanceByKey("Process_ORCH_Ciclo_Receita", variables);

        // Skip to SUB_06: Glosa Management
        assertThat(orchestrator).isWaitingAt("CallActivity_SUB_06_GlosaManagement");

        // Process glosa
        complete(task(), withVariables(
            "glosaType", "TECHNICAL",
            "glosaCode", "001",
            "glosaRecoverable", true,
            "requiredAction", "CORRECT_CODING"
        ));

        // Prepare appeal
        complete(task(), withVariables(
            "appealPrepared", true,
            "appealDocuments", Arrays.asList("Corrected coding", "Medical justification")
        ));

        // Submit appeal
        complete(task(), withVariables(
            "appealSubmitted", true,
            "appealProtocol", "APPEAL123"
        ));

        // Appeal approved
        complete(task(), withVariables(
            "appealStatus", "APPROVED",
            "recoveredAmount", new BigDecimal("500.00")
        ));

        // Assert - Glosa resolved
        Map<String, Object> vars = runtimeService().getVariables(orchestrator.getId());
        assertThat(vars)
            .containsEntry("appealStatus", "APPROVED")
            .containsKey("recoveredAmount");
    }

    @Test
    @Deployment(resources = "bpmn/ORCH_Ciclo_Receita_Hospital_Futuro.bpmn")
    @DisplayName("Should complete full cycle within expected time bounds")
    void shouldCompleteFullCycleWithinExpectedTimeBounds() {
        // Arrange
        long startTime = System.currentTimeMillis();

        Map<String, Object> variables = new HashMap<>();
        variables.put("patientCPF", "12345678900");
        variables.put("serviceType", "CONSULTATION");

        // Act
        ProcessInstance orchestrator = runtimeService()
            .startProcessInstanceByKey("Process_ORCH_Ciclo_Receita", variables);

        // Complete all tasks rapidly
        while (!orchestrator.isEnded() && task() != null) {
            complete(task(), withVariables(
                "status", "COMPLETED",
                "paymentReceived", true
            ));
        }

        long duration = System.currentTimeMillis() - startTime;

        // Assert - Should complete in reasonable time
        assertThat(duration).isLessThan(10000); // 10 seconds for full cycle
    }

    @Test
    @Deployment(resources = "bpmn/ORCH_Ciclo_Receita_Hospital_Futuro.bpmn")
    @DisplayName("Should handle process cancellation at any stage")
    void shouldHandleProcessCancellationAtAnyStage() {
        // Arrange
        Map<String, Object> variables = new HashMap<>();
        variables.put("patientCPF", "12345678900");

        // Act
        ProcessInstance orchestrator = runtimeService()
            .startProcessInstanceByKey("Process_ORCH_Ciclo_Receita", variables);

        // Cancel mid-process
        runtimeService().createMessageCorrelation("ProcessCancellation")
            .processInstanceId(orchestrator.getId())
            .setVariable("cancellationReason", "Patient request")
            .correlate();

        // Assert - Process handles cancellation gracefully
        assertThat(orchestrator).isNotActive()
            .or()
            .isWaitingAt("Task_HandleCancellation");
    }

    @Test
    @Deployment(resources = "bpmn/ORCH_Ciclo_Receita_Hospital_Futuro.bpmn")
    @DisplayName("Should track all subprocess completions in orchestrator")
    void shouldTrackAllSubprocessCompletionsInOrchestrator() {
        // Arrange
        Map<String, Object> variables = new HashMap<>();
        variables.put("patientCPF", "12345678900");

        // Act
        ProcessInstance orchestrator = runtimeService()
            .startProcessInstanceByKey("Process_ORCH_Ciclo_Receita", variables);

        // Complete each subprocess
        String[] subprocesses = {
            "SUB_01", "SUB_02", "SUB_03", "SUB_04", "SUB_05", "SUB_06", "SUB_07"
        };

        for (String subprocess : subprocesses) {
            if (task() != null) {
                complete(task(), withVariables(subprocess + "_completed", true));
            }
        }

        // Assert - All subprocesses tracked
        Map<String, Object> vars = historyService()
            .createHistoricVariableInstanceQuery()
            .processInstanceId(orchestrator.getId())
            .list()
            .stream()
            .filter(v -> v.getName().endsWith("_completed"))
            .collect(java.util.stream.Collectors.toMap(
                v -> v.getName(),
                v -> v.getValue()
            ));

        assertThat(vars).hasSizeGreaterThanOrEqualTo(5);
    }
}
