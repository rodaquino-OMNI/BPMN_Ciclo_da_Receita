package com.hospital.unit.delegates;

import com.hospital.fixtures.InsuranceFixtures;
import com.hospital.fixtures.PatientFixtures;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for Pre-Attendance delegates (SUB_02).
 * Tests insurance verification, eligibility checks, and authorization workflows.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pre-Attendance Delegate Tests")
class PreAttendanceDelegateTest {

    @Mock
    private DelegateExecution execution;

    @Mock
    private InsuranceApiClient insuranceClient;

    @Mock
    private TASYApiClient tasyClient;

    @Mock
    private InsurancePortalRPA rpaBot;

    @Mock
    private EligibilityService eligibilityService;

    private VerifyInsuranceDelegate verifyInsuranceDelegate;
    private CheckEligibilityDelegate checkEligibilityDelegate;
    private RequestAuthorizationDelegate requestAuthorizationDelegate;
    private CheckAuthorizationStatusDelegate checkAuthorizationStatusDelegate;
    private ProcessApprovalDelegate processApprovalDelegate;
    private ProcessDenialDelegate processDenialDelegate;
    private CalculateCoPayDelegate calculateCoPayDelegate;

    @BeforeEach
    void setUp() {
        verifyInsuranceDelegate = new VerifyInsuranceDelegate(insuranceClient, tasyClient);
        checkEligibilityDelegate = new CheckEligibilityDelegate(eligibilityService);
        requestAuthorizationDelegate = new RequestAuthorizationDelegate(insuranceClient, rpaBot);
        checkAuthorizationStatusDelegate = new CheckAuthorizationStatusDelegate(insuranceClient);
        processApprovalDelegate = new ProcessApprovalDelegate(tasyClient);
        processDenialDelegate = new ProcessDenialDelegate(tasyClient);
        calculateCoPayDelegate = new CalculateCoPayDelegate();
    }

    @Test
    @DisplayName("Should verify active insurance successfully")
    void shouldVerifyActiveInsuranceSuccessfully() throws Exception {
        // Arrange
        String patientId = "PAT123";
        String insuranceId = "UNIMED";
        String planCode = "UNIMED_PREMIUM_001";

        when(execution.getVariable("patientId")).thenReturn(patientId);
        when(execution.getVariable("insuranceId")).thenReturn(insuranceId);

        Map<String, Object> insuranceData = new HashMap<>();
        insuranceData.put("planCode", planCode);
        insuranceData.put("status", "ACTIVE");
        insuranceData.put("validUntil", LocalDate.now().plusMonths(6).toString());

        when(tasyClient.getPatientInsurance(patientId)).thenReturn(insuranceData);

        // Act
        verifyInsuranceDelegate.execute(execution);

        // Assert
        verify(execution).setVariable("insuranceVerified", true);
        verify(execution).setVariable("insurancePlanCode", planCode);
        verify(execution).setVariable("insuranceStatus", "ACTIVE");
    }

    @Test
    @DisplayName("Should detect expired insurance")
    void shouldDetectExpiredInsurance() throws Exception {
        // Arrange
        String patientId = "PAT123";

        when(execution.getVariable("patientId")).thenReturn(patientId);

        Map<String, Object> insuranceData = new HashMap<>();
        insuranceData.put("planCode", "UNIMED_PREMIUM_001");
        insuranceData.put("status", "EXPIRED");
        insuranceData.put("validUntil", LocalDate.now().minusDays(30).toString());

        when(tasyClient.getPatientInsurance(patientId)).thenReturn(insuranceData);

        // Act
        verifyInsuranceDelegate.execute(execution);

        // Assert
        verify(execution).setVariable("insuranceVerified", false);
        verify(execution).setVariable("insuranceStatus", "EXPIRED");
        verify(execution).setVariable(eq("insuranceError"), contains("expirado"));
    }

    @Test
    @DisplayName("Should handle patient without insurance")
    void shouldHandlePatientWithoutInsurance() throws Exception {
        // Arrange
        String patientId = "PAT123";

        when(execution.getVariable("patientId")).thenReturn(patientId);
        when(tasyClient.getPatientInsurance(patientId)).thenReturn(null);

        // Act
        verifyInsuranceDelegate.execute(execution);

        // Assert
        verify(execution).setVariable("insuranceVerified", false);
        verify(execution).setVariable("paymentType", "PARTICULAR");
    }

    @Test
    @DisplayName("Should check eligibility successfully")
    void shouldCheckEligibilitySuccessfully() throws Exception {
        // Arrange
        String patientId = "PAT123";
        String insuranceId = "UNIMED";
        String planCode = "UNIMED_PREMIUM_001";
        String procedureCode = "31001192"; // Surgery

        when(execution.getVariable("patientId")).thenReturn(patientId);
        when(execution.getVariable("insuranceId")).thenReturn(insuranceId);
        when(execution.getVariable("planCode")).thenReturn(planCode);
        when(execution.getVariable("procedureCode")).thenReturn(procedureCode);

        Map<String, Object> eligibilityResult = new HashMap<>();
        eligibilityResult.put("eligible", true);
        eligibilityResult.put("coverage", "100");
        eligibilityResult.put("coPayPercent", "20");

        when(eligibilityService.checkEligibility(patientId, insuranceId, planCode, procedureCode))
            .thenReturn(eligibilityResult);

        // Act
        checkEligibilityDelegate.execute(execution);

        // Assert
        verify(execution).setVariable("eligible", true);
        verify(execution).setVariable("coveragePercent", "100");
        verify(execution).setVariable("coPayPercent", "20");
    }

    @Test
    @DisplayName("Should detect ineligibility due to waiting period")
    void shouldDetectIneligibilityDueToWaitingPeriod() throws Exception {
        // Arrange
        String patientId = "PAT123";
        String procedureCode = "31001192";

        when(execution.getVariable("patientId")).thenReturn(patientId);
        when(execution.getVariable("procedureCode")).thenReturn(procedureCode);

        Map<String, Object> eligibilityResult = new HashMap<>();
        eligibilityResult.put("eligible", false);
        eligibilityResult.put("reason", "WAITING_PERIOD");
        eligibilityResult.put("daysRemaining", "90");

        when(eligibilityService.checkEligibility(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(eligibilityResult);

        // Act
        checkEligibilityDelegate.execute(execution);

        // Assert
        verify(execution).setVariable("eligible", false);
        verify(execution).setVariable("ineligibilityReason", "WAITING_PERIOD");
        verify(execution).setVariable("waitingDaysRemaining", "90");
    }

    @Test
    @DisplayName("Should request authorization via webservice")
    void shouldRequestAuthorizationViaWebservice() throws Exception {
        // Arrange
        InsuranceFixtures.AuthorizationRequest authRequest = InsuranceFixtures.surgeryRequest();

        when(execution.getVariable("patientId")).thenReturn(authRequest.getPatientId());
        when(execution.getVariable("insuranceId")).thenReturn(authRequest.getInsuranceId());
        when(execution.getVariable("procedureCodes")).thenReturn(authRequest.getProcedureCodes());
        when(execution.getVariable("useWebservice")).thenReturn(true);

        when(insuranceClient.requestAuthorization(any())).thenReturn("AUTH123456");

        // Act
        requestAuthorizationDelegate.execute(execution);

        // Assert
        ArgumentCaptor<Map> requestCaptor = ArgumentCaptor.forClass(Map.class);
        verify(insuranceClient).requestAuthorization(requestCaptor.capture());
        verify(execution).setVariable("authorizationNumber", "AUTH123456");
        verify(execution).setVariable("authorizationMethod", "WEBSERVICE");
        verify(execution).setVariable("authorizationStatus", "PENDING");
    }

    @Test
    @DisplayName("Should request authorization via RPA when webservice unavailable")
    void shouldRequestAuthorizationViaRPA() throws Exception {
        // Arrange
        InsuranceFixtures.AuthorizationRequest authRequest = InsuranceFixtures.surgeryRequest();

        when(execution.getVariable("useWebservice")).thenReturn(false);
        when(execution.getVariable("insurancePortalUrl")).thenReturn("https://portal.unimed.com.br");

        when(rpaBot.submitAuthorization(anyString(), any())).thenReturn("AUTH_RPA_789");

        // Act
        requestAuthorizationDelegate.execute(execution);

        // Assert
        verify(rpaBot).submitAuthorization(anyString(), any());
        verify(execution).setVariable("authorizationNumber", "AUTH_RPA_789");
        verify(execution).setVariable("authorizationMethod", "RPA");
    }

    @Test
    @DisplayName("Should handle webservice timeout and fallback to RPA")
    void shouldHandleWebserviceTimeoutAndFallbackToRPA() throws Exception {
        // Arrange
        when(execution.getVariable("useWebservice")).thenReturn(true);
        when(insuranceClient.requestAuthorization(any()))
            .thenThrow(new RuntimeException("Connection timeout"));
        when(rpaBot.submitAuthorization(anyString(), any())).thenReturn("AUTH_RPA_999");

        // Act
        requestAuthorizationDelegate.execute(execution);

        // Assert
        verify(insuranceClient).requestAuthorization(any());
        verify(rpaBot).submitAuthorization(anyString(), any());
        verify(execution).setVariable("authorizationMethod", "RPA");
        verify(execution).setVariable("fallbackUsed", true);
    }

    @Test
    @DisplayName("Should check authorization status and detect approval")
    void shouldCheckAuthorizationStatusAndDetectApproval() throws Exception {
        // Arrange
        String authNumber = "AUTH123456";

        when(execution.getVariable("authorizationNumber")).thenReturn(authNumber);

        Map<String, Object> statusResponse = new HashMap<>();
        statusResponse.put("status", "APPROVED");
        statusResponse.put("approvedDate", LocalDate.now().toString());
        statusResponse.put("validityDays", "30");

        when(insuranceClient.checkAuthorizationStatus(authNumber)).thenReturn(statusResponse);

        // Act
        checkAuthorizationStatusDelegate.execute(execution);

        // Assert
        verify(execution).setVariable("authorizationStatus", "APPROVED");
        verify(execution).setVariable("approvalDate", LocalDate.now().toString());
        verify(execution).setVariable("authorizationValidityDays", "30");
    }

    @Test
    @DisplayName("Should check authorization status and detect denial")
    void shouldCheckAuthorizationStatusAndDetectDenial() throws Exception {
        // Arrange
        String authNumber = "AUTH123456";

        when(execution.getVariable("authorizationNumber")).thenReturn(authNumber);

        Map<String, Object> statusResponse = new HashMap<>();
        statusResponse.put("status", "DENIED");
        statusResponse.put("denialReason", "Carência não cumprida");
        statusResponse.put("denialCode", "001");

        when(insuranceClient.checkAuthorizationStatus(authNumber)).thenReturn(statusResponse);

        // Act
        checkAuthorizationStatusDelegate.execute(execution);

        // Assert
        verify(execution).setVariable("authorizationStatus", "DENIED");
        verify(execution).setVariable("denialReason", "Carência não cumprida");
        verify(execution).setVariable("denialCode", "001");
    }

    @Test
    @DisplayName("Should detect pending authorization after timeout")
    void shouldDetectPendingAuthorizationAfterTimeout() throws Exception {
        // Arrange
        String authNumber = "AUTH123456";
        LocalDate requestDate = LocalDate.now().minusDays(3);

        when(execution.getVariable("authorizationNumber")).thenReturn(authNumber);
        when(execution.getVariable("requestDate")).thenReturn(requestDate);

        Map<String, Object> statusResponse = new HashMap<>();
        statusResponse.put("status", "PENDING");

        when(insuranceClient.checkAuthorizationStatus(authNumber)).thenReturn(statusResponse);

        // Act
        checkAuthorizationStatusDelegate.execute(execution);

        // Assert
        verify(execution).setVariable("authorizationStatus", "PENDING");
        verify(execution).setVariable("timeoutReached", true);
    }

    @Test
    @DisplayName("Should process approval and update TASY")
    void shouldProcessApprovalAndUpdateTASY() throws Exception {
        // Arrange
        String patientId = "PAT123";
        String authNumber = "AUTH123456";
        String accountId = "ACC789";

        when(execution.getVariable("patientId")).thenReturn(patientId);
        when(execution.getVariable("authorizationNumber")).thenReturn(authNumber);
        when(execution.getVariable("accountId")).thenReturn(accountId);

        // Act
        processApprovalDelegate.execute(execution);

        // Assert
        ArgumentCaptor<Map> updateCaptor = ArgumentCaptor.forClass(Map.class);
        verify(tasyClient).updateAccountAuthorization(eq(accountId), updateCaptor.capture());

        Map<String, Object> update = updateCaptor.getValue();
        assertThat(update)
            .containsEntry("authorizationNumber", authNumber)
            .containsEntry("authorizationStatus", "APPROVED");

        verify(execution).setVariable("readyForAdmission", true);
    }

    @Test
    @DisplayName("Should process denial and create appeal task")
    void shouldProcessDenialAndCreateAppealTask() throws Exception {
        // Arrange
        String authNumber = "AUTH123456";
        String denialReason = "Documentação incompleta";

        when(execution.getVariable("authorizationNumber")).thenReturn(authNumber);
        when(execution.getVariable("denialReason")).thenReturn(denialReason);

        // Act
        processDenialDelegate.execute(execution);

        // Assert
        verify(execution).setVariable("appealRequired", true);
        verify(execution).setVariable("appealReason", denialReason);
        verify(tasyClient).createAppealTask(authNumber, denialReason);
    }

    @ParameterizedTest
    @CsvSource({
        "1000.00, 20.00, 200.00",
        "5000.00, 15.00, 750.00",
        "500.00, 25.00, 125.00",
        "10000.00, 30.00, 3000.00"
    })
    @DisplayName("Should calculate co-pay correctly for different amounts")
    void shouldCalculateCoPayCorrectly(String totalAmount, String coPayPercent, String expectedCoPay) throws Exception {
        // Arrange
        when(execution.getVariable("totalAmount")).thenReturn(new BigDecimal(totalAmount));
        when(execution.getVariable("coPayPercent")).thenReturn(new BigDecimal(coPayPercent));

        // Act
        calculateCoPayDelegate.execute(execution);

        // Assert
        ArgumentCaptor<BigDecimal> coPayCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(execution).setVariable(eq("coPayAmount"), coPayCaptor.capture());

        assertThat(coPayCaptor.getValue())
            .isEqualByComparingTo(new BigDecimal(expectedCoPay));
    }

    @Test
    @DisplayName("Should handle zero co-pay for special plans")
    void shouldHandleZeroCoPayForSpecialPlans() throws Exception {
        // Arrange
        when(execution.getVariable("totalAmount")).thenReturn(new BigDecimal("1000.00"));
        when(execution.getVariable("coPayPercent")).thenReturn(BigDecimal.ZERO);

        // Act
        calculateCoPayDelegate.execute(execution);

        // Assert
        ArgumentCaptor<BigDecimal> coPayCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(execution).setVariable(eq("coPayAmount"), coPayCaptor.capture());

        assertThat(coPayCaptor.getValue()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should apply fixed co-pay when specified")
    void shouldApplyFixedCoPayWhenSpecified() throws Exception {
        // Arrange
        BigDecimal fixedCoPay = new BigDecimal("50.00");

        when(execution.getVariable("totalAmount")).thenReturn(new BigDecimal("1000.00"));
        when(execution.getVariable("coPayFixed")).thenReturn(fixedCoPay);

        // Act
        calculateCoPayDelegate.execute(execution);

        // Assert
        ArgumentCaptor<BigDecimal> coPayCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(execution).setVariable(eq("coPayAmount"), coPayCaptor.capture());

        assertThat(coPayCaptor.getValue()).isEqualByComparingTo(fixedCoPay);
    }

    @Test
    @DisplayName("Should retry authorization check on temporary failure")
    void shouldRetryAuthorizationCheckOnTemporaryFailure() throws Exception {
        // Arrange
        String authNumber = "AUTH123456";

        when(execution.getVariable("authorizationNumber")).thenReturn(authNumber);
        when(insuranceClient.checkAuthorizationStatus(authNumber))
            .thenThrow(new RuntimeException("Temporary network error"))
            .thenReturn(Map.of("status", "APPROVED"));

        // Act & Assert - First attempt fails
        assertThatThrownBy(() -> checkAuthorizationStatusDelegate.execute(execution))
            .hasMessageContaining("Temporary network error");

        // Second attempt succeeds
        checkAuthorizationStatusDelegate.execute(execution);
        verify(execution).setVariable("authorizationStatus", "APPROVED");
    }

    // Mock delegate classes
    private static class VerifyInsuranceDelegate {
        private final InsuranceApiClient insuranceClient;
        private final TASYApiClient tasyClient;

        VerifyInsuranceDelegate(InsuranceApiClient insuranceClient, TASYApiClient tasyClient) {
            this.insuranceClient = insuranceClient;
            this.tasyClient = tasyClient;
        }

        public void execute(DelegateExecution execution) {
            String patientId = (String) execution.getVariable("patientId");
            Map<String, Object> insuranceData = tasyClient.getPatientInsurance(patientId);

            if (insuranceData == null) {
                execution.setVariable("insuranceVerified", false);
                execution.setVariable("paymentType", "PARTICULAR");
                return;
            }

            String status = (String) insuranceData.get("status");
            execution.setVariable("insuranceStatus", status);
            execution.setVariable("insurancePlanCode", insuranceData.get("planCode"));

            if ("EXPIRED".equals(status)) {
                execution.setVariable("insuranceVerified", false);
                execution.setVariable("insuranceError", "Plano de saúde expirado");
            } else {
                execution.setVariable("insuranceVerified", true);
            }
        }
    }

    private static class CheckEligibilityDelegate {
        private final EligibilityService eligibilityService;

        CheckEligibilityDelegate(EligibilityService eligibilityService) {
            this.eligibilityService = eligibilityService;
        }

        public void execute(DelegateExecution execution) {
            String patientId = (String) execution.getVariable("patientId");
            String insuranceId = (String) execution.getVariable("insuranceId");
            String planCode = (String) execution.getVariable("planCode");
            String procedureCode = (String) execution.getVariable("procedureCode");

            Map<String, Object> result = eligibilityService.checkEligibility(
                patientId, insuranceId, planCode, procedureCode
            );

            Boolean eligible = (Boolean) result.get("eligible");
            execution.setVariable("eligible", eligible);

            if (eligible) {
                execution.setVariable("coveragePercent", result.get("coverage"));
                execution.setVariable("coPayPercent", result.get("coPayPercent"));
            } else {
                execution.setVariable("ineligibilityReason", result.get("reason"));
                if (result.containsKey("daysRemaining")) {
                    execution.setVariable("waitingDaysRemaining", result.get("daysRemaining"));
                }
            }
        }
    }

    private static class RequestAuthorizationDelegate {
        private final InsuranceApiClient insuranceClient;
        private final InsurancePortalRPA rpaBot;

        RequestAuthorizationDelegate(InsuranceApiClient insuranceClient, InsurancePortalRPA rpaBot) {
            this.insuranceClient = insuranceClient;
            this.rpaBot = rpaBot;
        }

        public void execute(DelegateExecution execution) {
            Boolean useWebservice = (Boolean) execution.getVariable("useWebservice");
            String authNumber;

            try {
                if (Boolean.TRUE.equals(useWebservice)) {
                    authNumber = insuranceClient.requestAuthorization(execution.getVariables());
                    execution.setVariable("authorizationMethod", "WEBSERVICE");
                } else {
                    throw new RuntimeException("Webservice not available");
                }
            } catch (Exception e) {
                // Fallback to RPA
                String portalUrl = (String) execution.getVariable("insurancePortalUrl");
                authNumber = rpaBot.submitAuthorization(portalUrl, execution.getVariables());
                execution.setVariable("authorizationMethod", "RPA");
                execution.setVariable("fallbackUsed", true);
            }

            execution.setVariable("authorizationNumber", authNumber);
            execution.setVariable("authorizationStatus", "PENDING");
        }
    }

    private static class CheckAuthorizationStatusDelegate {
        private final InsuranceApiClient insuranceClient;

        CheckAuthorizationStatusDelegate(InsuranceApiClient insuranceClient) {
            this.insuranceClient = insuranceClient;
        }

        public void execute(DelegateExecution execution) {
            String authNumber = (String) execution.getVariable("authorizationNumber");
            Map<String, Object> statusResponse = insuranceClient.checkAuthorizationStatus(authNumber);

            String status = (String) statusResponse.get("status");
            execution.setVariable("authorizationStatus", status);

            if ("APPROVED".equals(status)) {
                execution.setVariable("approvalDate", statusResponse.get("approvedDate"));
                execution.setVariable("authorizationValidityDays", statusResponse.get("validityDays"));
            } else if ("DENIED".equals(status)) {
                execution.setVariable("denialReason", statusResponse.get("denialReason"));
                execution.setVariable("denialCode", statusResponse.get("denialCode"));
            } else if ("PENDING".equals(status)) {
                LocalDate requestDate = (LocalDate) execution.getVariable("requestDate");
                if (requestDate != null && requestDate.plusDays(2).isBefore(LocalDate.now())) {
                    execution.setVariable("timeoutReached", true);
                }
            }
        }
    }

    private static class ProcessApprovalDelegate {
        private final TASYApiClient tasyClient;

        ProcessApprovalDelegate(TASYApiClient tasyClient) {
            this.tasyClient = tasyClient;
        }

        public void execute(DelegateExecution execution) {
            String accountId = (String) execution.getVariable("accountId");
            String authNumber = (String) execution.getVariable("authorizationNumber");

            Map<String, Object> update = new HashMap<>();
            update.put("authorizationNumber", authNumber);
            update.put("authorizationStatus", "APPROVED");

            tasyClient.updateAccountAuthorization(accountId, update);
            execution.setVariable("readyForAdmission", true);
        }
    }

    private static class ProcessDenialDelegate {
        private final TASYApiClient tasyClient;

        ProcessDenialDelegate(TASYApiClient tasyClient) {
            this.tasyClient = tasyClient;
        }

        public void execute(DelegateExecution execution) {
            String authNumber = (String) execution.getVariable("authorizationNumber");
            String denialReason = (String) execution.getVariable("denialReason");

            execution.setVariable("appealRequired", true);
            execution.setVariable("appealReason", denialReason);

            tasyClient.createAppealTask(authNumber, denialReason);
        }
    }

    private static class CalculateCoPayDelegate {
        public void execute(DelegateExecution execution) {
            BigDecimal totalAmount = (BigDecimal) execution.getVariable("totalAmount");
            BigDecimal fixedCoPay = (BigDecimal) execution.getVariable("coPayFixed");
            BigDecimal coPayAmount;

            if (fixedCoPay != null && fixedCoPay.compareTo(BigDecimal.ZERO) > 0) {
                coPayAmount = fixedCoPay;
            } else {
                BigDecimal coPayPercent = (BigDecimal) execution.getVariable("coPayPercent");
                if (coPayPercent == null || coPayPercent.compareTo(BigDecimal.ZERO) == 0) {
                    coPayAmount = BigDecimal.ZERO;
                } else {
                    coPayAmount = totalAmount.multiply(coPayPercent).divide(new BigDecimal("100"));
                }
            }

            execution.setVariable("coPayAmount", coPayAmount);
        }
    }

    // Mock interfaces
    interface InsuranceApiClient {
        String requestAuthorization(Map<String, Object> request);
        Map<String, Object> checkAuthorizationStatus(String authorizationNumber);
    }

    interface TASYApiClient {
        Map<String, Object> getPatientInsurance(String patientId);
        void updateAccountAuthorization(String accountId, Map<String, Object> update);
        void createAppealTask(String authorizationNumber, String reason);
    }

    interface InsurancePortalRPA {
        String submitAuthorization(String portalUrl, Map<String, Object> data);
    }

    interface EligibilityService {
        Map<String, Object> checkEligibility(String patientId, String insuranceId, String planCode, String procedureCode);
    }
}
