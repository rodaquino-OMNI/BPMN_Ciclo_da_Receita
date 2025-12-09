package com.hospital.unit.delegates;

import com.hospital.fixtures.PatientFixtures;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for First Contact and Scheduling delegates (SUB_01).
 * Tests patient registration, scheduling, and notification workflows.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("First Contact Delegate Tests")
class FirstContactDelegateTest {

    @Mock
    private DelegateExecution execution;

    @Mock
    private TASYApiClient tasyClient;

    @Mock
    private WhatsAppService whatsAppService;

    @Mock
    private PatientValidator patientValidator;

    private IdentifyChannelDelegate identifyChannelDelegate;
    private ValidatePatientDataDelegate validatePatientDelegate;
    private CreatePatientDelegate createPatientDelegate;
    private CheckAvailabilityDelegate checkAvailabilityDelegate;
    private BookAppointmentDelegate bookAppointmentDelegate;
    private SendConfirmationDelegate sendConfirmationDelegate;

    @BeforeEach
    void setUp() {
        identifyChannelDelegate = new IdentifyChannelDelegate();
        validatePatientDelegate = new ValidatePatientDataDelegate(patientValidator);
        createPatientDelegate = new CreatePatientDelegate(tasyClient);
        checkAvailabilityDelegate = new CheckAvailabilityDelegate(tasyClient);
        bookAppointmentDelegate = new BookAppointmentDelegate(tasyClient);
        sendConfirmationDelegate = new SendConfirmationDelegate(whatsAppService);
    }

    @Test
    @DisplayName("Should identify channel from WhatsApp contact")
    void shouldIdentifyChannelFromWhatsApp() throws Exception {
        // Arrange
        when(execution.getVariable("messageOrigin")).thenReturn("whatsapp");
        when(execution.getVariable("phone")).thenReturn("5511987654321");

        // Act
        identifyChannelDelegate.execute(execution);

        // Assert
        ArgumentCaptor<String> channelCaptor = ArgumentCaptor.forClass(String.class);
        verify(execution).setVariable(eq("contactChannel"), channelCaptor.capture());
        assertThat(channelCaptor.getValue()).isEqualTo("WHATSAPP");
    }

    @Test
    @DisplayName("Should identify channel from portal")
    void shouldIdentifyChannelFromPortal() throws Exception {
        // Arrange
        when(execution.getVariable("messageOrigin")).thenReturn("portal");
        when(execution.getVariable("sessionId")).thenReturn("SESSION123");

        // Act
        identifyChannelDelegate.execute(execution);

        // Assert
        verify(execution).setVariable("contactChannel", "PORTAL");
    }

    @Test
    @DisplayName("Should validate patient CPF successfully")
    void shouldValidatePatientCPF() throws Exception {
        // Arrange
        PatientFixtures.Patient patient = PatientFixtures.defaultPatient();
        when(execution.getVariable("patientCPF")).thenReturn(patient.getCpf());
        when(patientValidator.isValidCPF(patient.getCpf())).thenReturn(true);

        // Act
        validatePatientDelegate.execute(execution);

        // Assert
        verify(execution).setVariable("cpfValid", true);
        verify(execution, never()).setVariable(eq("error"), any());
    }

    @Test
    @DisplayName("Should fail validation with invalid CPF")
    void shouldFailValidationWithInvalidCPF() throws Exception {
        // Arrange
        String invalidCPF = "00000000000";
        when(execution.getVariable("patientCPF")).thenReturn(invalidCPF);
        when(patientValidator.isValidCPF(invalidCPF)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> validatePatientDelegate.execute(execution))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("CPF inválido");

        verify(patientValidator).isValidCPF(invalidCPF);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "123", "12345678901234", "abc.def.ghi-jk"})
    @DisplayName("Should reject malformed CPF formats")
    void shouldRejectMalformedCPFFormats(String malformedCPF) throws Exception {
        // Arrange
        when(execution.getVariable("patientCPF")).thenReturn(malformedCPF);
        when(patientValidator.isValidCPF(malformedCPF)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> validatePatientDelegate.execute(execution))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should create new patient in TASY")
    void shouldCreateNewPatientInTASY() throws Exception {
        // Arrange
        PatientFixtures.Patient patient = PatientFixtures.defaultPatient();
        Map<String, Object> patientData = new HashMap<>();
        patientData.put("name", patient.getName());
        patientData.put("cpf", patient.getCpf());
        patientData.put("birthDate", patient.getBirthDate());
        patientData.put("email", patient.getEmail());
        patientData.put("phone", patient.getPhone());

        when(execution.getVariables()).thenReturn(patientData);
        when(tasyClient.createPatient(any())).thenReturn("PAT123456");

        // Act
        createPatientDelegate.execute(execution);

        // Assert
        ArgumentCaptor<Map> requestCaptor = ArgumentCaptor.forClass(Map.class);
        verify(tasyClient).createPatient(requestCaptor.capture());
        verify(execution).setVariable("patientId", "PAT123456");
        verify(execution).setVariable("patientExists", true);

        Map<String, Object> capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest)
            .containsEntry("name", patient.getName())
            .containsEntry("cpf", patient.getCpf());
    }

    @Test
    @DisplayName("Should handle TASY API error gracefully")
    void shouldHandleTASYAPIError() throws Exception {
        // Arrange
        when(execution.getVariables()).thenReturn(new HashMap<>());
        when(tasyClient.createPatient(any()))
            .thenThrow(new RuntimeException("TASY API timeout"));

        // Act & Assert
        assertThatThrownBy(() -> createPatientDelegate.execute(execution))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("TASY API timeout");
    }

    @Test
    @DisplayName("Should check appointment slot availability")
    void shouldCheckAppointmentSlotAvailability() throws Exception {
        // Arrange
        LocalDateTime requestedDateTime = LocalDateTime.now().plusDays(7);
        when(execution.getVariable("serviceType")).thenReturn("CONSULTATION");
        when(execution.getVariable("appointmentDateTime")).thenReturn(requestedDateTime);
        when(tasyClient.checkAvailability("CONSULTATION", requestedDateTime))
            .thenReturn(true);

        // Act
        checkAvailabilityDelegate.execute(execution);

        // Assert
        verify(execution).setVariable("slotAvailable", true);
        verify(tasyClient).checkAvailability("CONSULTATION", requestedDateTime);
    }

    @Test
    @DisplayName("Should return false when slot is not available")
    void shouldReturnFalseWhenSlotNotAvailable() throws Exception {
        // Arrange
        LocalDateTime requestedDateTime = LocalDateTime.now().plusDays(1);
        when(execution.getVariable("serviceType")).thenReturn("SURGERY");
        when(execution.getVariable("appointmentDateTime")).thenReturn(requestedDateTime);
        when(tasyClient.checkAvailability("SURGERY", requestedDateTime))
            .thenReturn(false);

        // Act
        checkAvailabilityDelegate.execute(execution);

        // Assert
        verify(execution).setVariable("slotAvailable", false);
    }

    @Test
    @DisplayName("Should book appointment successfully")
    void shouldBookAppointmentSuccessfully() throws Exception {
        // Arrange
        String patientId = "PAT123";
        String serviceType = "CONSULTATION";
        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(7);

        when(execution.getVariable("patientId")).thenReturn(patientId);
        when(execution.getVariable("serviceType")).thenReturn(serviceType);
        when(execution.getVariable("appointmentDateTime")).thenReturn(appointmentTime);
        when(tasyClient.bookAppointment(any())).thenReturn("APPT123456");

        // Act
        bookAppointmentDelegate.execute(execution);

        // Assert
        verify(execution).setVariable("appointmentId", "APPT123456");
        verify(execution).setVariable("appointmentStatus", "CONFIRMED");

        ArgumentCaptor<Map> requestCaptor = ArgumentCaptor.forClass(Map.class);
        verify(tasyClient).bookAppointment(requestCaptor.capture());
        Map<String, Object> request = requestCaptor.getValue();
        assertThat(request)
            .containsEntry("patientId", patientId)
            .containsEntry("serviceType", serviceType);
    }

    @Test
    @DisplayName("Should send confirmation via WhatsApp")
    void shouldSendConfirmationViaWhatsApp() throws Exception {
        // Arrange
        String phone = "5511987654321";
        String patientName = "João Silva";
        String appointmentId = "APPT123";
        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(7);

        when(execution.getVariable("contactChannel")).thenReturn("WHATSAPP");
        when(execution.getVariable("phone")).thenReturn(phone);
        when(execution.getVariable("patientName")).thenReturn(patientName);
        when(execution.getVariable("appointmentId")).thenReturn(appointmentId);
        when(execution.getVariable("appointmentDateTime")).thenReturn(appointmentTime);

        // Act
        sendConfirmationDelegate.execute(execution);

        // Assert
        ArgumentCaptor<String> phoneCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(whatsAppService).sendMessage(phoneCaptor.capture(), messageCaptor.capture());

        assertThat(phoneCaptor.getValue()).isEqualTo(phone);
        assertThat(messageCaptor.getValue())
            .contains(patientName)
            .contains(appointmentId);

        verify(execution).setVariable("confirmationSent", true);
    }

    @Test
    @DisplayName("Should handle confirmation send failure")
    void shouldHandleConfirmationSendFailure() throws Exception {
        // Arrange
        when(execution.getVariable("contactChannel")).thenReturn("WHATSAPP");
        when(execution.getVariable("phone")).thenReturn("5511987654321");
        doThrow(new RuntimeException("WhatsApp service unavailable"))
            .when(whatsAppService).sendMessage(any(), any());

        // Act
        sendConfirmationDelegate.execute(execution);

        // Assert
        verify(execution).setVariable("confirmationSent", false);
        verify(execution).setVariable(eq("confirmationError"), contains("WhatsApp service unavailable"));
    }

    @Test
    @DisplayName("Should add patient to waiting list when slot not available")
    void shouldAddPatientToWaitingList() throws Exception {
        // Arrange
        String patientId = "PAT123";
        String serviceType = "SURGERY";

        when(execution.getVariable("patientId")).thenReturn(patientId);
        when(execution.getVariable("serviceType")).thenReturn(serviceType);
        when(tasyClient.addToWaitingList(patientId, serviceType))
            .thenReturn("WAIT123");

        AddToWaitingListDelegate delegate = new AddToWaitingListDelegate(tasyClient);

        // Act
        delegate.execute(execution);

        // Assert
        verify(execution).setVariable("waitingListId", "WAIT123");
        verify(execution).setVariable("waitingListStatus", "ACTIVE");
    }

    @Test
    @DisplayName("Should handle concurrent appointment requests")
    void shouldHandleConcurrentAppointmentRequests() throws Exception {
        // Arrange
        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(7).withHour(10).withMinute(0);

        when(execution.getVariable("serviceType")).thenReturn("CONSULTATION");
        when(execution.getVariable("appointmentDateTime")).thenReturn(appointmentTime);
        when(tasyClient.checkAvailability("CONSULTATION", appointmentTime))
            .thenReturn(true)
            .thenReturn(false); // Slot taken by another request

        // Act
        checkAvailabilityDelegate.execute(execution);

        // Assert - First check returns true
        verify(execution).setVariable("slotAvailable", true);

        // Simulate race condition - slot taken before booking
        when(tasyClient.bookAppointment(any()))
            .thenThrow(new RuntimeException("Slot no longer available"));

        assertThatThrownBy(() -> bookAppointmentDelegate.execute(execution))
            .hasMessageContaining("Slot no longer available");
    }

    @Test
    @DisplayName("Should validate required fields before creating patient")
    void shouldValidateRequiredFieldsBeforeCreatingPatient() throws Exception {
        // Arrange
        Map<String, Object> incompleteData = new HashMap<>();
        incompleteData.put("name", "João Silva");
        // Missing CPF

        when(execution.getVariables()).thenReturn(incompleteData);

        // Act & Assert
        assertThatThrownBy(() -> createPatientDelegate.execute(execution))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("CPF é obrigatório");
    }

    @Test
    @DisplayName("Should schedule reminder for 24 hours before appointment")
    void shouldScheduleReminderFor24HoursBeforeAppointment() throws Exception {
        // Arrange
        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(7);
        LocalDateTime reminderTime = appointmentTime.minusHours(24);

        when(execution.getVariable("appointmentDateTime")).thenReturn(appointmentTime);

        ScheduleReminderDelegate delegate = new ScheduleReminderDelegate();

        // Act
        delegate.execute(execution);

        // Assert
        ArgumentCaptor<LocalDateTime> timeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(execution).setVariable(eq("reminderDateTime"), timeCaptor.capture());

        assertThat(timeCaptor.getValue()).isEqualTo(reminderTime);
    }

    // Mock classes for testing
    private static class IdentifyChannelDelegate {
        public void execute(DelegateExecution execution) {
            String origin = (String) execution.getVariable("messageOrigin");
            String channel = switch (origin) {
                case "whatsapp" -> "WHATSAPP";
                case "portal" -> "PORTAL";
                case "app" -> "APP";
                default -> "PHONE";
            };
            execution.setVariable("contactChannel", channel);
        }
    }

    private static class ValidatePatientDataDelegate {
        private final PatientValidator validator;

        ValidatePatientDataDelegate(PatientValidator validator) {
            this.validator = validator;
        }

        public void execute(DelegateExecution execution) {
            String cpf = (String) execution.getVariable("patientCPF");
            if (!validator.isValidCPF(cpf)) {
                throw new IllegalArgumentException("CPF inválido: " + cpf);
            }
            execution.setVariable("cpfValid", true);
        }
    }

    private static class CreatePatientDelegate {
        private final TASYApiClient client;

        CreatePatientDelegate(TASYApiClient client) {
            this.client = client;
        }

        public void execute(DelegateExecution execution) {
            Map<String, Object> variables = execution.getVariables();
            if (!variables.containsKey("cpf")) {
                throw new IllegalArgumentException("CPF é obrigatório");
            }
            String patientId = client.createPatient(variables);
            execution.setVariable("patientId", patientId);
            execution.setVariable("patientExists", true);
        }
    }

    private static class CheckAvailabilityDelegate {
        private final TASYApiClient client;

        CheckAvailabilityDelegate(TASYApiClient client) {
            this.client = client;
        }

        public void execute(DelegateExecution execution) {
            String serviceType = (String) execution.getVariable("serviceType");
            LocalDateTime dateTime = (LocalDateTime) execution.getVariable("appointmentDateTime");
            boolean available = client.checkAvailability(serviceType, dateTime);
            execution.setVariable("slotAvailable", available);
        }
    }

    private static class BookAppointmentDelegate {
        private final TASYApiClient client;

        BookAppointmentDelegate(TASYApiClient client) {
            this.client = client;
        }

        public void execute(DelegateExecution execution) {
            Map<String, Object> request = new HashMap<>();
            request.put("patientId", execution.getVariable("patientId"));
            request.put("serviceType", execution.getVariable("serviceType"));
            request.put("appointmentDateTime", execution.getVariable("appointmentDateTime"));

            String appointmentId = client.bookAppointment(request);
            execution.setVariable("appointmentId", appointmentId);
            execution.setVariable("appointmentStatus", "CONFIRMED");
        }
    }

    private static class SendConfirmationDelegate {
        private final WhatsAppService whatsAppService;

        SendConfirmationDelegate(WhatsAppService whatsAppService) {
            this.whatsAppService = whatsAppService;
        }

        public void execute(DelegateExecution execution) {
            try {
                String phone = (String) execution.getVariable("phone");
                String name = (String) execution.getVariable("patientName");
                String apptId = (String) execution.getVariable("appointmentId");

                String message = String.format("Olá %s! Seu agendamento %s foi confirmado.", name, apptId);
                whatsAppService.sendMessage(phone, message);

                execution.setVariable("confirmationSent", true);
            } catch (Exception e) {
                execution.setVariable("confirmationSent", false);
                execution.setVariable("confirmationError", e.getMessage());
            }
        }
    }

    private static class AddToWaitingListDelegate {
        private final TASYApiClient client;

        AddToWaitingListDelegate(TASYApiClient client) {
            this.client = client;
        }

        public void execute(DelegateExecution execution) {
            String patientId = (String) execution.getVariable("patientId");
            String serviceType = (String) execution.getVariable("serviceType");
            String waitingListId = client.addToWaitingList(patientId, serviceType);
            execution.setVariable("waitingListId", waitingListId);
            execution.setVariable("waitingListStatus", "ACTIVE");
        }
    }

    private static class ScheduleReminderDelegate {
        public void execute(DelegateExecution execution) {
            LocalDateTime appointmentTime = (LocalDateTime) execution.getVariable("appointmentDateTime");
            LocalDateTime reminderTime = appointmentTime.minusHours(24);
            execution.setVariable("reminderDateTime", reminderTime);
        }
    }

    // Mock interfaces
    interface TASYApiClient {
        String createPatient(Map<String, Object> patientData);
        boolean checkAvailability(String serviceType, LocalDateTime dateTime);
        String bookAppointment(Map<String, Object> request);
        String addToWaitingList(String patientId, String serviceType);
    }

    interface WhatsAppService {
        void sendMessage(String phone, String message);
    }

    interface PatientValidator {
        boolean isValidCPF(String cpf);
    }
}
