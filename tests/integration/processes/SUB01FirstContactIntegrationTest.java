package com.hospital.integration.processes;

import com.hospital.fixtures.PatientFixtures;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.camunda.bpm.extension.junit5.test.ProcessEngineExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for SUB_01_Agendamento_Registro process.
 * Tests the complete first contact and scheduling workflow.
 */
@ExtendWith(ProcessEngineExtension.class)
@DisplayName("SUB_01 First Contact Integration Tests")
class SUB01FirstContactIntegrationTest {

    @Test
    @Deployment(resources = "bpmn/SUB_01_Agendamento_Registro.bpmn")
    @DisplayName("Should complete first contact process for WhatsApp channel")
    void shouldCompleteFirstContactProcessForWhatsApp() {
        // Arrange
        PatientFixtures.Patient patient = PatientFixtures.defaultPatient();

        Map<String, Object> variables = new HashMap<>();
        variables.put("messageOrigin", "whatsapp");
        variables.put("phone", patient.getPhone());
        variables.put("patientName", patient.getName());
        variables.put("patientCPF", patient.getCpf());
        variables.put("patientEmail", patient.getEmail());
        variables.put("serviceType", "CONSULTATION");
        variables.put("appointmentDateTime", LocalDateTime.now().plusDays(7));

        // Act
        ProcessInstance processInstance = runtimeService()
            .startProcessInstanceByKey("Process_SUB_01_First_Contact", variables);

        // Assert
        assertThat(processInstance).isStarted();
        assertThat(processInstance).isWaitingAt("Task_CheckAvailability");

        // Complete availability check
        complete(task(), withVariables("slotAvailable", true));

        // Should proceed to booking
        assertThat(processInstance).isWaitingAt("Task_BookAppointment");

        complete(task(), withVariables(
            "appointmentId", "APPT123",
            "appointmentStatus", "CONFIRMED"
        ));

        // Should send confirmation
        assertThat(processInstance).isWaitingAt("Task_SendConfirmation");

        complete(task(), withVariables("confirmationSent", true));

        // Process should complete
        assertThat(processInstance).isEnded();
    }

    @Test
    @Deployment(resources = "bpmn/SUB_01_Agendamento_Registro.bpmn")
    @DisplayName("Should add to waiting list when slot not available")
    void shouldAddToWaitingListWhenSlotNotAvailable() {
        // Arrange
        Map<String, Object> variables = new HashMap<>();
        variables.put("messageOrigin", "portal");
        variables.put("patientCPF", "12345678900");
        variables.put("serviceType", "SURGERY");

        // Act
        ProcessInstance processInstance = runtimeService()
            .startProcessInstanceByKey("Process_SUB_01_First_Contact", variables);

        // Slot not available
        complete(task(), withVariables("slotAvailable", false));

        // Should go to waiting list path
        assertThat(processInstance).isWaitingAt("Task_AddToWaitingList");

        complete(task(), withVariables(
            "waitingListId", "WAIT123",
            "waitingListStatus", "ACTIVE"
        ));

        // Should notify about waiting list
        assertThat(processInstance).isWaitingAt("Task_NotifyWaitingList");

        complete(task());

        assertThat(processInstance).isEnded();
    }

    @Test
    @Deployment(resources = "bpmn/SUB_01_Agendamento_Registro.bpmn")
    @DisplayName("Should handle new patient registration")
    void shouldHandleNewPatientRegistration() {
        // Arrange
        PatientFixtures.Patient newPatient = PatientFixtures.defaultPatient();

        Map<String, Object> variables = new HashMap<>();
        variables.put("patientCPF", newPatient.getCpf());
        variables.put("patientExists", false);
        variables.put("patientName", newPatient.getName());
        variables.put("birthDate", newPatient.getBirthDate());
        variables.put("email", newPatient.getEmail());
        variables.put("phone", newPatient.getPhone());

        // Act
        ProcessInstance processInstance = runtimeService()
            .startProcessInstanceByKey("Process_SUB_01_First_Contact", variables);

        // Should wait for patient creation
        assertThat(processInstance).isWaitingAt("Task_CreatePatient");

        complete(task(), withVariables(
            "patientId", "PAT_NEW_123",
            "patientCreated", true
        ));

        // Continue with scheduling
        assertThat(processInstance).isActive();
    }

    @Test
    @Deployment(resources = "bpmn/SUB_01_Agendamento_Registro.bpmn")
    @DisplayName("Should validate patient CPF before proceeding")
    void shouldValidatePatientCPFBeforeProceeding() {
        // Arrange
        Map<String, Object> variables = new HashMap<>();
        variables.put("patientCPF", "12345678900");

        // Act
        ProcessInstance processInstance = runtimeService()
            .startProcessInstanceByKey("Process_SUB_01_First_Contact", variables);

        // Should validate CPF
        assertThat(processInstance).isWaitingAt("Task_ValidateCPF");

        complete(task(), withVariables("cpfValid", true));

        // Should proceed
        assertThat(processInstance).isActive();
    }

    @Test
    @Deployment(resources = "bpmn/SUB_01_Agendamento_Registro.bpmn")
    @DisplayName("Should handle appointment cancellation")
    void shouldHandleAppointmentCancellation() {
        // Arrange
        Map<String, Object> variables = new HashMap<>();
        variables.put("appointmentId", "APPT123");
        variables.put("cancellationReason", "Patient requested");

        // Act - Trigger cancellation event
        ProcessInstance processInstance = runtimeService()
            .startProcessInstanceByKey("Process_SUB_01_First_Contact", variables);

        // Send cancellation message
        runtimeService().createMessageCorrelation("AppointmentCancellation")
            .processInstanceId(processInstance.getId())
            .correlate();

        // Should handle cancellation
        assertThat(processInstance).isWaitingAt("Task_ProcessCancellation");

        complete(task(), withVariables("cancellationProcessed", true));
    }

    @Test
    @Deployment(resources = "bpmn/SUB_01_Agendamento_Registro.bpmn")
    @DisplayName("Should send reminder 24 hours before appointment")
    void shouldSendReminder24HoursBeforeAppointment() {
        // Arrange
        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(2);
        Map<String, Object> variables = new HashMap<>();
        variables.put("appointmentId", "APPT123");
        variables.put("appointmentDateTime", appointmentTime);
        variables.put("phone", "11987654321");

        // Act
        ProcessInstance processInstance = runtimeService()
            .startProcessInstanceByKey("Process_SUB_01_First_Contact", variables);

        // Complete up to reminder scheduling
        complete(task()); // Various tasks...

        // Should schedule reminder
        assertThat(processInstance).hasVariables("reminderDateTime");
    }

    @Test
    @Deployment(resources = "bpmn/SUB_01_Agendamento_Registro.bpmn")
    @DisplayName("Should handle concurrent appointment requests for same slot")
    void shouldHandleConcurrentAppointmentRequests() {
        // Arrange
        LocalDateTime targetTime = LocalDateTime.now().plusDays(7).withHour(10).withMinute(0);

        Map<String, Object> variables1 = new HashMap<>();
        variables1.put("patientCPF", "11111111111");
        variables1.put("appointmentDateTime", targetTime);

        Map<String, Object> variables2 = new HashMap<>();
        variables2.put("patientCPF", "22222222222");
        variables2.put("appointmentDateTime", targetTime);

        // Act - Start two processes for same slot
        ProcessInstance process1 = runtimeService()
            .startProcessInstanceByKey("Process_SUB_01_First_Contact", variables1);
        ProcessInstance process2 = runtimeService()
            .startProcessInstanceByKey("Process_SUB_01_First_Contact", variables2);

        // First one should get the slot
        complete(task(process1), withVariables("slotAvailable", true));
        assertThat(process1).isWaitingAt("Task_BookAppointment");

        // Second one should see slot unavailable
        complete(task(process2), withVariables("slotAvailable", false));
        assertThat(process2).isWaitingAt("Task_AddToWaitingList");
    }

    @Test
    @Deployment(resources = "bpmn/SUB_01_Agendamento_Registro.bpmn")
    @DisplayName("Should complete process end-to-end within expected time")
    void shouldCompleteProcessEndToEndWithinExpectedTime() {
        // Arrange
        PatientFixtures.Patient patient = PatientFixtures.defaultPatient();
        Map<String, Object> variables = new HashMap<>();
        variables.put("patientCPF", patient.getCpf());
        variables.put("serviceType", "CONSULTATION");
        variables.put("appointmentDateTime", LocalDateTime.now().plusDays(7));

        long startTime = System.currentTimeMillis();

        // Act
        ProcessInstance processInstance = runtimeService()
            .startProcessInstanceByKey("Process_SUB_01_First_Contact", variables);

        // Complete all tasks
        while (!processInstance.isEnded()) {
            if (task() != null) {
                complete(task(), withVariables(
                    "slotAvailable", true,
                    "appointmentId", "APPT123",
                    "confirmationSent", true
                ));
            }
        }

        long duration = System.currentTimeMillis() - startTime;

        // Assert - Should complete in reasonable time
        assertThat(duration).isLessThan(5000); // 5 seconds
        assertThat(processInstance).isEnded();
    }
}
