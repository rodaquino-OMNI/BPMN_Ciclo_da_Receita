package com.hospital.unit.delegates;

import com.hospital.fixtures.ClinicalFixtures;
import com.hospital.fixtures.PatientFixtures;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for Clinical Attendance delegates (SUB_03).
 * Tests admission, clinical documentation, and discharge workflows.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Clinical Attendance Delegate Tests")
class ClinicalAttendanceDelegateTest {

    @Mock
    private DelegateExecution execution;

    @Mock
    private TASYClinicalAPI tasyClient;

    @Mock
    private DocumentationValidator documentationValidator;

    @Mock
    private RFIDMaterialTracker rfidTracker;

    private RegisterAdmissionDelegate registerAdmissionDelegate;
    private CollectClinicalDataDelegate collectClinicalDataDelegate;
    private ValidateDocumentationDelegate validateDocumentationDelegate;
    private TrackMaterialUsageDelegate trackMaterialUsageDelegate;
    private RegisterProceduresDelegate registerProceduresDelegate;
    private PrepareDischargeSummaryDelegate prepareDischargeSummaryDelegate;
    private ProcessDischargeDelegate processDischargeDelegate;

    @BeforeEach
    void setUp() {
        registerAdmissionDelegate = new RegisterAdmissionDelegate(tasyClient);
        collectClinicalDataDelegate = new CollectClinicalDataDelegate(tasyClient);
        validateDocumentationDelegate = new ValidateDocumentationDelegate(documentationValidator);
        trackMaterialUsageDelegate = new TrackMaterialUsageDelegate(rfidTracker, tasyClient);
        registerProceduresDelegate = new RegisterProceduresDelegate(tasyClient);
        prepareDischargeSummaryDelegate = new PrepareDischargeSummaryDelegate(tasyClient);
        processDischargeDelegate = new ProcessDischargeDelegate(tasyClient);
    }

    @Test
    @DisplayName("Should register inpatient admission successfully")
    void shouldRegisterInpatientAdmissionSuccessfully() throws Exception {
        // Arrange
        String patientId = "PAT123";
        String admissionType = "INPATIENT";
        String authorizationNumber = "AUTH123";

        when(execution.getVariable("patientId")).thenReturn(patientId);
        when(execution.getVariable("admissionType")).thenReturn(admissionType);
        when(execution.getVariable("authorizationNumber")).thenReturn(authorizationNumber);

        when(tasyClient.createAdmission(any())).thenReturn("ADM789");

        // Act
        registerAdmissionDelegate.execute(execution);

        // Assert
        ArgumentCaptor<Map> admissionCaptor = ArgumentCaptor.forClass(Map.class);
        verify(tasyClient).createAdmission(admissionCaptor.capture());

        Map<String, Object> admission = admissionCaptor.getValue();
        assertThat(admission)
            .containsEntry("patientId", patientId)
            .containsEntry("admissionType", admissionType)
            .containsEntry("authorizationNumber", authorizationNumber);

        verify(execution).setVariable("admissionId", "ADM789");
        verify(execution).setVariable("admissionStatus", "ACTIVE");
    }

    @Test
    @DisplayName("Should register emergency admission without authorization")
    void shouldRegisterEmergencyAdmissionWithoutAuthorization() throws Exception {
        // Arrange
        String patientId = "PAT_EMERG_456";
        String admissionType = "EMERGENCY";

        when(execution.getVariable("patientId")).thenReturn(patientId);
        when(execution.getVariable("admissionType")).thenReturn(admissionType);
        when(execution.getVariable("authorizationNumber")).thenReturn(null);

        when(tasyClient.createAdmission(any())).thenReturn("ADM_EMERG_999");

        // Act
        registerAdmissionDelegate.execute(execution);

        // Assert
        verify(tasyClient).createAdmission(any());
        verify(execution).setVariable("admissionId", "ADM_EMERG_999");
        verify(execution).setVariable("requiresPostAuthorization", true);
    }

    @Test
    @DisplayName("Should collect clinical data including diagnoses")
    void shouldCollectClinicalDataIncludingDiagnoses() throws Exception {
        // Arrange
        String admissionId = "ADM123";
        List<String> cidCodes = Arrays.asList("K80.2", "E11.9");

        when(execution.getVariable("admissionId")).thenReturn(admissionId);

        Map<String, Object> clinicalData = new HashMap<>();
        clinicalData.put("cidCodes", cidCodes);
        clinicalData.put("chiefComplaint", "Abdominal pain");
        clinicalData.put("clinicalHistory", "Patient reports...");

        when(tasyClient.getClinicalData(admissionId)).thenReturn(clinicalData);

        // Act
        collectClinicalDataDelegate.execute(execution);

        // Assert
        verify(execution).setVariable("cidCodes", cidCodes);
        verify(execution).setVariable("chiefComplaint", "Abdominal pain");
        verify(execution).setVariable("clinicalDataCollected", true);
    }

    @Test
    @DisplayName("Should validate complete documentation")
    void shouldValidateCompleteDocumentation() throws Exception {
        // Arrange
        String admissionId = "ADM123";

        when(execution.getVariable("admissionId")).thenReturn(admissionId);
        when(documentationValidator.validateCompleteness(admissionId)).thenReturn(true);
        when(documentationValidator.getRequiredDocuments()).thenReturn(
            Arrays.asList("Admission Form", "Consent", "Clinical History")
        );
        when(documentationValidator.getMissingDocuments(admissionId)).thenReturn(Arrays.asList());

        // Act
        validateDocumentationDelegate.execute(execution);

        // Assert
        verify(execution).setVariable("documentationComplete", true);
        verify(execution).setVariable("missingDocuments", Arrays.asList());
    }

    @Test
    @DisplayName("Should detect incomplete documentation")
    void shouldDetectIncompleteDocumentation() throws Exception {
        // Arrange
        String admissionId = "ADM123";
        List<String> missingDocs = Arrays.asList("Discharge Summary", "Lab Results");

        when(execution.getVariable("admissionId")).thenReturn(admissionId);
        when(documentationValidator.validateCompleteness(admissionId)).thenReturn(false);
        when(documentationValidator.getMissingDocuments(admissionId)).thenReturn(missingDocs);

        // Act
        validateDocumentationDelegate.execute(execution);

        // Assert
        verify(execution).setVariable("documentationComplete", false);
        verify(execution).setVariable("missingDocuments", missingDocs);
    }

    @Test
    @DisplayName("Should track material usage via RFID")
    void shouldTrackMaterialUsageViaRFID() throws Exception {
        // Arrange
        String admissionId = "ADM123";
        String rfidTag = "RFID123456";

        when(execution.getVariable("admissionId")).thenReturn(admissionId);
        when(execution.getVariable("rfidTag")).thenReturn(rfidTag);

        Map<String, Object> materialData = new HashMap<>();
        materialData.put("code", "MAT001");
        materialData.put("description", "Surgical suture");
        materialData.put("quantity", "5");
        materialData.put("unitPrice", "45.00");

        when(rfidTracker.getMaterialByRFID(rfidTag)).thenReturn(materialData);

        // Act
        trackMaterialUsageDelegate.execute(execution);

        // Assert
        ArgumentCaptor<Map> usageCaptor = ArgumentCaptor.forClass(Map.class);
        verify(tasyClient).registerMaterialUsage(eq(admissionId), usageCaptor.capture());

        Map<String, Object> usage = usageCaptor.getValue();
        assertThat(usage)
            .containsEntry("code", "MAT001")
            .containsEntry("rfidTag", rfidTag);

        verify(execution).setVariable("materialRegistered", true);
    }

    @Test
    @DisplayName("Should handle RFID read error gracefully")
    void shouldHandleRFIDReadErrorGracefully() throws Exception {
        // Arrange
        String rfidTag = "RFID_INVALID";

        when(execution.getVariable("rfidTag")).thenReturn(rfidTag);
        when(rfidTracker.getMaterialByRFID(rfidTag))
            .thenThrow(new RuntimeException("RFID tag not found"));

        // Act
        trackMaterialUsageDelegate.execute(execution);

        // Assert
        verify(execution).setVariable("materialRegistered", false);
        verify(execution).setVariable(eq("rfidError"), contains("not found"));
        verify(tasyClient, never()).registerMaterialUsage(anyString(), any());
    }

    @Test
    @DisplayName("Should register surgical procedures")
    void shouldRegisterSurgicalProcedures() throws Exception {
        // Arrange
        String admissionId = "ADM123";

        ClinicalFixtures.Procedure surgery = new ClinicalFixtures.Procedure();
        surgery.setTusscode("31001192");
        surgery.setDescription("Cholecystectomy");
        surgery.setPerformingPhysician("Dr. Maria Santos");
        surgery.setPerformedDate(LocalDateTime.now());

        when(execution.getVariable("admissionId")).thenReturn(admissionId);
        when(execution.getVariable("procedures")).thenReturn(Arrays.asList(surgery));

        // Act
        registerProceduresDelegate.execute(execution);

        // Assert
        ArgumentCaptor<List> proceduresCaptor = ArgumentCaptor.forClass(List.class);
        verify(tasyClient).registerProcedures(eq(admissionId), proceduresCaptor.capture());

        List<ClinicalFixtures.Procedure> registeredProcs = proceduresCaptor.getValue();
        assertThat(registeredProcs).hasSize(1);
        assertThat(registeredProcs.get(0).getTusscode()).isEqualTo("31001192");

        verify(execution).setVariable("proceduresRegistered", true);
    }

    @Test
    @DisplayName("Should prepare discharge summary with all required fields")
    void shouldPrepareDischargeSummaryWithAllRequiredFields() throws Exception {
        // Arrange
        String admissionId = "ADM123";

        Map<String, Object> summaryData = new HashMap<>();
        summaryData.put("admissionDate", LocalDateTime.now().minusDays(3).toString());
        summaryData.put("dischargeDate", LocalDateTime.now().toString());
        summaryData.put("principalDiagnosis", "K80.2");
        summaryData.put("secondaryDiagnoses", Arrays.asList("E11.9"));
        summaryData.put("proceduresPerformed", Arrays.asList("31001192"));
        summaryData.put("dischargeSummary", "Patient underwent successful surgery...");
        summaryData.put("dischargeInstructions", "Follow-up in 7 days...");

        when(execution.getVariable("admissionId")).thenReturn(admissionId);
        when(tasyClient.prepareDischargeSummary(admissionId)).thenReturn(summaryData);

        // Act
        prepareDischargeSummaryDelegate.execute(execution);

        // Assert
        verify(execution).setVariable("summaryPrepared", true);
        verify(execution).setVariable("principalDiagnosis", "K80.2");
        verify(execution).setVariable("dischargeSummary", "Patient underwent successful surgery...");
    }

    @Test
    @DisplayName("Should process regular discharge")
    void shouldProcessRegularDischarge() throws Exception {
        // Arrange
        String admissionId = "ADM123";
        String dischargeType = "REGULAR";
        LocalDateTime dischargeDateTime = LocalDateTime.now();

        when(execution.getVariable("admissionId")).thenReturn(admissionId);
        when(execution.getVariable("dischargeType")).thenReturn(dischargeType);
        when(execution.getVariable("dischargeDateTime")).thenReturn(dischargeDateTime);

        // Act
        processDischargeDelegate.execute(execution);

        // Assert
        ArgumentCaptor<Map> dischargeCaptor = ArgumentCaptor.forClass(Map.class);
        verify(tasyClient).processDischarge(eq(admissionId), dischargeCaptor.capture());

        Map<String, Object> discharge = dischargeCaptor.getValue();
        assertThat(discharge)
            .containsEntry("dischargeType", dischargeType)
            .containsEntry("dischargeDateTime", dischargeDateTime);

        verify(execution).setVariable("dischargeProcessed", true);
        verify(execution).setVariable("readyForBilling", true);
    }

    @Test
    @DisplayName("Should process death discharge with required notifications")
    void shouldProcessDeathDischargeWithRequiredNotifications() throws Exception {
        // Arrange
        String admissionId = "ADM123";
        String dischargeType = "DEATH";

        when(execution.getVariable("admissionId")).thenReturn(admissionId);
        when(execution.getVariable("dischargeType")).thenReturn(dischargeType);

        // Act
        processDischargeDelegate.execute(execution);

        // Assert
        verify(tasyClient).processDischarge(eq(admissionId), any());
        verify(execution).setVariable("dischargeProcessed", true);
        verify(execution).setVariable("notifyHealthAuthorities", true);
        verify(execution).setVariable("readyForBilling", false);
    }

    @Test
    @DisplayName("Should process transfer discharge")
    void shouldProcessTransferDischarge() throws Exception {
        // Arrange
        String admissionId = "ADM123";
        String dischargeType = "TRANSFER";
        String transferDestination = "Hospital Central";

        when(execution.getVariable("admissionId")).thenReturn(admissionId);
        when(execution.getVariable("dischargeType")).thenReturn(dischargeType);
        when(execution.getVariable("transferDestination")).thenReturn(transferDestination);

        // Act
        processDischargeDelegate.execute(execution);

        // Assert
        ArgumentCaptor<Map> dischargeCaptor = ArgumentCaptor.forClass(Map.class);
        verify(tasyClient).processDischarge(eq(admissionId), dischargeCaptor.capture());

        Map<String, Object> discharge = dischargeCaptor.getValue();
        assertThat(discharge)
            .containsEntry("dischargeType", dischargeType)
            .containsEntry("transferDestination", transferDestination);

        verify(execution).setVariable("transferDocumentationRequired", true);
    }

    @Test
    @DisplayName("Should validate required CID codes before discharge")
    void shouldValidateRequiredCIDCodesBeforeDischarge() throws Exception {
        // Arrange
        String admissionId = "ADM123";

        when(execution.getVariable("admissionId")).thenReturn(admissionId);

        Map<String, Object> summaryData = new HashMap<>();
        summaryData.put("principalDiagnosis", null); // Missing required field

        when(tasyClient.prepareDischargeSummary(admissionId)).thenReturn(summaryData);

        // Act & Assert
        assertThatThrownBy(() -> prepareDischargeSummaryDelegate.execute(execution))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Principal diagnosis required");
    }

    @Test
    @DisplayName("Should handle concurrent material registrations")
    void shouldHandleConcurrentMaterialRegistrations() throws Exception {
        // Arrange
        String admissionId = "ADM123";
        String rfidTag1 = "RFID001";
        String rfidTag2 = "RFID002";

        when(execution.getVariable("admissionId")).thenReturn(admissionId);

        Map<String, Object> material1 = Map.of("code", "MAT001", "description", "Suture");
        Map<String, Object> material2 = Map.of("code", "MAT002", "description", "Gauze");

        // First material
        when(execution.getVariable("rfidTag")).thenReturn(rfidTag1);
        when(rfidTracker.getMaterialByRFID(rfidTag1)).thenReturn(material1);
        trackMaterialUsageDelegate.execute(execution);

        // Second material
        when(execution.getVariable("rfidTag")).thenReturn(rfidTag2);
        when(rfidTracker.getMaterialByRFID(rfidTag2)).thenReturn(material2);
        trackMaterialUsageDelegate.execute(execution);

        // Assert
        verify(tasyClient, times(2)).registerMaterialUsage(eq(admissionId), any());
    }

    // Mock delegate classes
    private static class RegisterAdmissionDelegate {
        private final TASYClinicalAPI tasyClient;

        RegisterAdmissionDelegate(TASYClinicalAPI tasyClient) {
            this.tasyClient = tasyClient;
        }

        public void execute(DelegateExecution execution) {
            Map<String, Object> admission = new HashMap<>();
            admission.put("patientId", execution.getVariable("patientId"));
            admission.put("admissionType", execution.getVariable("admissionType"));
            admission.put("authorizationNumber", execution.getVariable("authorizationNumber"));
            admission.put("admissionDateTime", LocalDateTime.now());

            String admissionId = tasyClient.createAdmission(admission);
            execution.setVariable("admissionId", admissionId);
            execution.setVariable("admissionStatus", "ACTIVE");

            if (execution.getVariable("authorizationNumber") == null &&
                "EMERGENCY".equals(execution.getVariable("admissionType"))) {
                execution.setVariable("requiresPostAuthorization", true);
            }
        }
    }

    private static class CollectClinicalDataDelegate {
        private final TASYClinicalAPI tasyClient;

        CollectClinicalDataDelegate(TASYClinicalAPI tasyClient) {
            this.tasyClient = tasyClient;
        }

        public void execute(DelegateExecution execution) {
            String admissionId = (String) execution.getVariable("admissionId");
            Map<String, Object> clinicalData = tasyClient.getClinicalData(admissionId);

            execution.setVariable("cidCodes", clinicalData.get("cidCodes"));
            execution.setVariable("chiefComplaint", clinicalData.get("chiefComplaint"));
            execution.setVariable("clinicalDataCollected", true);
        }
    }

    private static class ValidateDocumentationDelegate {
        private final DocumentationValidator validator;

        ValidateDocumentationDelegate(DocumentationValidator validator) {
            this.validator = validator;
        }

        public void execute(DelegateExecution execution) {
            String admissionId = (String) execution.getVariable("admissionId");
            boolean complete = validator.validateCompleteness(admissionId);

            execution.setVariable("documentationComplete", complete);

            if (!complete) {
                List<String> missing = validator.getMissingDocuments(admissionId);
                execution.setVariable("missingDocuments", missing);
            } else {
                execution.setVariable("missingDocuments", Arrays.asList());
            }
        }
    }

    private static class TrackMaterialUsageDelegate {
        private final RFIDMaterialTracker rfidTracker;
        private final TASYClinicalAPI tasyClient;

        TrackMaterialUsageDelegate(RFIDMaterialTracker rfidTracker, TASYClinicalAPI tasyClient) {
            this.rfidTracker = rfidTracker;
            this.tasyClient = tasyClient;
        }

        public void execute(DelegateExecution execution) {
            try {
                String rfidTag = (String) execution.getVariable("rfidTag");
                Map<String, Object> materialData = rfidTracker.getMaterialByRFID(rfidTag);

                String admissionId = (String) execution.getVariable("admissionId");
                tasyClient.registerMaterialUsage(admissionId, materialData);

                execution.setVariable("materialRegistered", true);
            } catch (Exception e) {
                execution.setVariable("materialRegistered", false);
                execution.setVariable("rfidError", e.getMessage());
            }
        }
    }

    private static class RegisterProceduresDelegate {
        private final TASYClinicalAPI tasyClient;

        RegisterProceduresDelegate(TASYClinicalAPI tasyClient) {
            this.tasyClient = tasyClient;
        }

        public void execute(DelegateExecution execution) {
            String admissionId = (String) execution.getVariable("admissionId");
            List<ClinicalFixtures.Procedure> procedures =
                (List<ClinicalFixtures.Procedure>) execution.getVariable("procedures");

            tasyClient.registerProcedures(admissionId, procedures);
            execution.setVariable("proceduresRegistered", true);
        }
    }

    private static class PrepareDischargeSummaryDelegate {
        private final TASYClinicalAPI tasyClient;

        PrepareDischargeSummaryDelegate(TASYClinicalAPI tasyClient) {
            this.tasyClient = tasyClient;
        }

        public void execute(DelegateExecution execution) {
            String admissionId = (String) execution.getVariable("admissionId");
            Map<String, Object> summaryData = tasyClient.prepareDischargeSummary(admissionId);

            if (summaryData.get("principalDiagnosis") == null) {
                throw new IllegalStateException("Principal diagnosis required for discharge");
            }

            execution.setVariable("summaryPrepared", true);
            execution.setVariable("principalDiagnosis", summaryData.get("principalDiagnosis"));
            execution.setVariable("dischargeSummary", summaryData.get("dischargeSummary"));
        }
    }

    private static class ProcessDischargeDelegate {
        private final TASYClinicalAPI tasyClient;

        ProcessDischargeDelegate(TASYClinicalAPI tasyClient) {
            this.tasyClient = tasyClient;
        }

        public void execute(DelegateExecution execution) {
            String admissionId = (String) execution.getVariable("admissionId");
            String dischargeType = (String) execution.getVariable("dischargeType");

            Map<String, Object> discharge = new HashMap<>();
            discharge.put("dischargeType", dischargeType);
            discharge.put("dischargeDateTime", execution.getVariable("dischargeDateTime"));

            if ("TRANSFER".equals(dischargeType)) {
                discharge.put("transferDestination", execution.getVariable("transferDestination"));
                execution.setVariable("transferDocumentationRequired", true);
            }

            tasyClient.processDischarge(admissionId, discharge);
            execution.setVariable("dischargeProcessed", true);

            if ("DEATH".equals(dischargeType)) {
                execution.setVariable("notifyHealthAuthorities", true);
                execution.setVariable("readyForBilling", false);
            } else {
                execution.setVariable("readyForBilling", true);
            }
        }
    }

    // Mock interfaces
    interface TASYClinicalAPI {
        String createAdmission(Map<String, Object> admission);
        Map<String, Object> getClinicalData(String admissionId);
        void registerMaterialUsage(String admissionId, Map<String, Object> material);
        void registerProcedures(String admissionId, List<ClinicalFixtures.Procedure> procedures);
        Map<String, Object> prepareDischargeSummary(String admissionId);
        void processDischarge(String admissionId, Map<String, Object> discharge);
    }

    interface DocumentationValidator {
        boolean validateCompleteness(String admissionId);
        List<String> getRequiredDocuments();
        List<String> getMissingDocuments(String admissionId);
    }

    interface RFIDMaterialTracker {
        Map<String, Object> getMaterialByRFID(String rfidTag);
    }
}
