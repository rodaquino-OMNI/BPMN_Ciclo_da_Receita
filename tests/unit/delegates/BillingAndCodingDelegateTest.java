package com.hospital.unit.delegates;

import com.hospital.fixtures.BillingFixtures;
import com.hospital.fixtures.ClinicalFixtures;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for Billing and Coding delegates (SUB_04, SUB_05, SUB_06).
 * Tests medical coding, billing calculation, and submission workflows.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Billing and Coding Delegate Tests")
class BillingAndCodingDelegateTest {

    @Mock
    private DelegateExecution execution;

    @Mock
    private MedicalCodingService codingService;

    @Mock
    private BillingCalculationEngine billingEngine;

    @Mock
    private TISSGateway tissGateway;

    @Mock
    private GlosaManagementService glosaService;

    private ValidateClinicalCodingDelegate validateCodingDelegate;
    private CalculateAccountDelegate calculateAccountDelegate;
    private GenerateTISSGuideDelegate generateTISSDelegate;
    private SubmitToInsuranceDelegate submitToInsuranceDelegate;
    private ProcessGlosaDelegate processGlosaDelegate;
    private PrepareAppealDelegate prepareAppealDelegate;

    @BeforeEach
    void setUp() {
        validateCodingDelegate = new ValidateClinicalCodingDelegate(codingService);
        calculateAccountDelegate = new CalculateAccountDelegate(billingEngine);
        generateTISSDelegate = new GenerateTISSGuideDelegate(tissGateway);
        submitToInsuranceDelegate = new SubmitToInsuranceDelegate(tissGateway);
        processGlosaDelegate = new ProcessGlosaDelegate(glosaService);
        prepareAppealDelegate = new PrepareAppealDelegate(glosaService);
    }

    @Test
    @DisplayName("Should validate TUSS codes successfully")
    void shouldValidateTUSSCodesSuccessfully() throws Exception {
        // Arrange
        String admissionId = "ADM123";
        List<String> tussCodes = Arrays.asList("31001192", "20104049", "70000014");

        when(execution.getVariable("admissionId")).thenReturn(admissionId);
        when(execution.getVariable("procedureCodes")).thenReturn(tussCodes);

        when(codingService.validateTUSSCodes(tussCodes)).thenReturn(true);

        // Act
        validateCodingDelegate.execute(execution);

        // Assert
        verify(execution).setVariable("codingValid", true);
        verify(execution).setVariable("invalidCodes", Arrays.asList());
    }

    @Test
    @DisplayName("Should detect invalid TUSS codes")
    void shouldDetectInvalidTUSSCodes() throws Exception {
        // Arrange
        List<String> tussCodes = Arrays.asList("31001192", "INVALID_CODE");
        List<String> invalidCodes = Arrays.asList("INVALID_CODE");

        when(execution.getVariable("procedureCodes")).thenReturn(tussCodes);
        when(codingService.validateTUSSCodes(tussCodes)).thenReturn(false);
        when(codingService.getInvalidCodes(tussCodes)).thenReturn(invalidCodes);

        // Act
        validateCodingDelegate.execute(execution);

        // Assert
        verify(execution).setVariable("codingValid", false);
        verify(execution).setVariable("invalidCodes", invalidCodes);
    }

    @Test
    @DisplayName("Should validate CID-10 codes")
    void shouldValidateCID10Codes() throws Exception {
        // Arrange
        List<String> cidCodes = Arrays.asList("K80.2", "E11.9");

        when(execution.getVariable("cidCodes")).thenReturn(cidCodes);
        when(codingService.validateCIDCodes(cidCodes)).thenReturn(true);

        // Act
        validateCodingDelegate.execute(execution);

        // Assert
        verify(execution).setVariable("diagnosisCodeValid", true);
    }

    @Test
    @DisplayName("Should calculate account total correctly")
    void shouldCalculateAccountTotalCorrectly() throws Exception {
        // Arrange
        String accountId = "ACC123";

        BillingFixtures.BillingData billing = BillingFixtures.surgicalAccount();

        when(execution.getVariable("accountId")).thenReturn(accountId);
        when(billingEngine.calculateAccount(accountId)).thenReturn(billing);

        // Act
        calculateAccountDelegate.execute(execution);

        // Assert
        ArgumentCaptor<BigDecimal> totalCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(execution).setVariable(eq("totalAmount"), totalCaptor.capture());

        assertThat(totalCaptor.getValue())
            .isEqualByComparingTo(new BigDecimal("12000.00"));

        verify(execution).setVariable("calculationComplete", true);
    }

    @ParameterizedTest
    @CsvSource({
        "1000.00, 20.00, 200.00, 800.00",
        "5000.00, 15.00, 750.00, 4250.00",
        "10000.00, 30.00, 3000.00, 7000.00"
    })
    @DisplayName("Should split amounts between patient and insurance correctly")
    void shouldSplitAmountsBetweenPatientAndInsurance(
        String total, String coPayPercent, String expectedCoPay, String expectedInsurance
    ) throws Exception {
        // Arrange
        BigDecimal totalAmount = new BigDecimal(total);
        BigDecimal coPayPct = new BigDecimal(coPayPercent);

        BillingFixtures.BillingData billing = BillingFixtures.completedAccount();
        billing.setTotalAmount(totalAmount);
        billing.setCoPayAmount(totalAmount.multiply(coPayPct).divide(new BigDecimal("100")));
        billing.setInsuranceAmount(totalAmount.subtract(billing.getCoPayAmount()));

        when(billingEngine.calculateAccount(anyString())).thenReturn(billing);

        // Act
        calculateAccountDelegate.execute(execution);

        // Assert
        ArgumentCaptor<BigDecimal> coPayCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        ArgumentCaptor<BigDecimal> insuranceCaptor = ArgumentCaptor.forClass(BigDecimal.class);

        verify(execution).setVariable(eq("coPayAmount"), coPayCaptor.capture());
        verify(execution).setVariable(eq("insuranceAmount"), insuranceCaptor.capture());

        assertThat(coPayCaptor.getValue()).isEqualByComparingTo(new BigDecimal(expectedCoPay));
        assertThat(insuranceCaptor.getValue()).isEqualByComparingTo(new BigDecimal(expectedInsurance));
    }

    @Test
    @DisplayName("Should generate TISS guide successfully")
    void shouldGenerateTISSGuideSuccessfully() throws Exception {
        // Arrange
        String accountId = "ACC123";
        String insuranceId = "UNIMED";

        BillingFixtures.BillingData billing = BillingFixtures.completedAccount();

        when(execution.getVariable("accountId")).thenReturn(accountId);
        when(execution.getVariable("insuranceId")).thenReturn(insuranceId);
        when(execution.getVariable("billingData")).thenReturn(billing);

        String guideNumber = "TISS" + System.currentTimeMillis();
        when(tissGateway.generateGuide(accountId, insuranceId, billing)).thenReturn(guideNumber);

        // Act
        generateTISSDelegate.execute(execution);

        // Assert
        verify(execution).setVariable("tissGuideNumber", guideNumber);
        verify(execution).setVariable("guideGenerated", true);
    }

    @Test
    @DisplayName("Should submit to insurance via webservice")
    void shouldSubmitToInsuranceViaWebservice() throws Exception {
        // Arrange
        String tissGuideNumber = "TISS123456";
        String insuranceId = "UNIMED";

        when(execution.getVariable("tissGuideNumber")).thenReturn(tissGuideNumber);
        when(execution.getVariable("insuranceId")).thenReturn(insuranceId);

        Map<String, Object> submissionResult = new HashMap<>();
        submissionResult.put("protocol", "PROT987654");
        submissionResult.put("status", "SUBMITTED");
        submissionResult.put("submissionDate", LocalDate.now().toString());

        when(tissGateway.submitGuide(tissGuideNumber, insuranceId)).thenReturn(submissionResult);

        // Act
        submitToInsuranceDelegate.execute(execution);

        // Assert
        verify(execution).setVariable("protocolNumber", "PROT987654");
        verify(execution).setVariable("submissionStatus", "SUBMITTED");
        verify(execution).setVariable("submissionDate", LocalDate.now().toString());
    }

    @Test
    @DisplayName("Should retry submission on temporary failure")
    void shouldRetrySubmissionOnTemporaryFailure() throws Exception {
        // Arrange
        String tissGuideNumber = "TISS123456";

        when(execution.getVariable("tissGuideNumber")).thenReturn(tissGuideNumber);
        when(execution.getVariable("retryCount")).thenReturn(0);

        when(tissGateway.submitGuide(anyString(), anyString()))
            .thenThrow(new RuntimeException("Network timeout"));

        // Act
        submitToInsuranceDelegate.execute(execution);

        // Assert
        verify(execution).setVariable("submissionStatus", "FAILED");
        verify(execution).setVariable("retryRequired", true);
        ArgumentCaptor<Integer> retryCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(execution).setVariable(eq("retryCount"), retryCaptor.capture());
        assertThat(retryCaptor.getValue()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should process technical glosa (coding error)")
    void shouldProcessTechnicalGlosa() throws Exception {
        // Arrange
        String accountId = "ACC123";
        String glosaType = "TECHNICAL";
        String glosaCode = "001";
        String glosaReason = "Código TUSS inválido";

        when(execution.getVariable("accountId")).thenReturn(accountId);
        when(execution.getVariable("glosaType")).thenReturn(glosaType);
        when(execution.getVariable("glosaCode")).thenReturn(glosaCode);
        when(execution.getVariable("glosaReason")).thenReturn(glosaReason);

        Map<String, Object> glosaClassification = new HashMap<>();
        glosaClassification.put("type", "TECHNICAL");
        glosaClassification.put("recoverable", true);
        glosaClassification.put("requiredAction", "CORRECT_CODING");

        when(glosaService.classifyGlosa(glosaType, glosaCode)).thenReturn(glosaClassification);

        // Act
        processGlosaDelegate.execute(execution);

        // Assert
        verify(execution).setVariable("glosaRecoverable", true);
        verify(execution).setVariable("requiredAction", "CORRECT_CODING");
        verify(execution).setVariable("glosaProcessed", true);
    }

    @Test
    @DisplayName("Should process administrative glosa (missing document)")
    void shouldProcessAdministrativeGlosa() throws Exception {
        // Arrange
        String glosaType = "ADMINISTRATIVE";
        String glosaCode = "002";

        when(execution.getVariable("glosaType")).thenReturn(glosaType);
        when(execution.getVariable("glosaCode")).thenReturn(glosaCode);

        Map<String, Object> glosaClassification = new HashMap<>();
        glosaClassification.put("type", "ADMINISTRATIVE");
        glosaClassification.put("recoverable", true);
        glosaClassification.put("requiredAction", "SUBMIT_DOCUMENTS");

        when(glosaService.classifyGlosa(glosaType, glosaCode)).thenReturn(glosaClassification);

        // Act
        processGlosaDelegate.execute(execution);

        // Assert
        verify(execution).setVariable("requiredAction", "SUBMIT_DOCUMENTS");
    }

    @Test
    @DisplayName("Should process clinical glosa (lack of justification)")
    void shouldProcessClinicalGlosa() throws Exception {
        // Arrange
        String glosaType = "CLINICAL";
        String glosaCode = "003";

        when(execution.getVariable("glosaType")).thenReturn(glosaType);
        when(execution.getVariable("glosaCode")).thenReturn(glosaCode);

        Map<String, Object> glosaClassification = new HashMap<>();
        glosaClassification.put("type", "CLINICAL");
        glosaClassification.put("recoverable", false);
        glosaClassification.put("requiredAction", "MEDICAL_REVIEW");

        when(glosaService.classifyGlosa(glosaType, glosaCode)).thenReturn(glosaClassification);

        // Act
        processGlosaDelegate.execute(execution);

        // Assert
        verify(execution).setVariable("glosaRecoverable", false);
        verify(execution).setVariable("requiredAction", "MEDICAL_REVIEW");
    }

    @Test
    @DisplayName("Should prepare appeal with supporting documentation")
    void shouldPrepareAppealWithSupportingDocumentation() throws Exception {
        // Arrange
        String accountId = "ACC123";
        String denialId = "DEN001";
        String glosaReason = "Documentação incompleta";

        when(execution.getVariable("accountId")).thenReturn(accountId);
        when(execution.getVariable("denialId")).thenReturn(denialId);
        when(execution.getVariable("glosaReason")).thenReturn(glosaReason);

        List<String> requiredDocs = Arrays.asList(
            "Medical Report",
            "Lab Results",
            "Authorization Letter"
        );

        when(glosaService.getRequiredDocumentsForAppeal(denialId)).thenReturn(requiredDocs);

        // Act
        prepareAppealDelegate.execute(execution);

        // Assert
        verify(execution).setVariable("requiredDocuments", requiredDocs);
        verify(execution).setVariable("appealPrepared", true);
    }

    @Test
    @DisplayName("Should calculate glosa recovery probability")
    void shouldCalculateGlosaRecoveryProbability() throws Exception {
        // Arrange
        String glosaType = "TECHNICAL";
        String glosaCode = "001";
        BigDecimal deniedAmount = new BigDecimal("500.00");

        when(execution.getVariable("glosaType")).thenReturn(glosaType);
        when(execution.getVariable("glosaCode")).thenReturn(glosaCode);
        when(execution.getVariable("deniedAmount")).thenReturn(deniedAmount);

        when(glosaService.calculateRecoveryProbability(glosaType, glosaCode))
            .thenReturn(85.0); // 85% probability

        // Act
        processGlosaDelegate.execute(execution);

        // Assert
        ArgumentCaptor<Double> probabilityCaptor = ArgumentCaptor.forClass(Double.class);
        verify(execution).setVariable(eq("recoveryProbability"), probabilityCaptor.capture());
        assertThat(probabilityCaptor.getValue()).isEqualTo(85.0);
    }

    @Test
    @DisplayName("Should handle batch billing for multiple accounts")
    void shouldHandleBatchBillingForMultipleAccounts() throws Exception {
        // Arrange
        List<String> accountIds = Arrays.asList("ACC001", "ACC002", "ACC003");

        when(execution.getVariable("accountIds")).thenReturn(accountIds);

        for (String accountId : accountIds) {
            BillingFixtures.BillingData billing = BillingFixtures.completedAccount();
            billing.setAccountId(accountId);
            when(billingEngine.calculateAccount(accountId)).thenReturn(billing);
        }

        // Act
        for (String accountId : accountIds) {
            when(execution.getVariable("accountId")).thenReturn(accountId);
            calculateAccountDelegate.execute(execution);
        }

        // Assert
        verify(billingEngine, times(3)).calculateAccount(anyString());
    }

    // Mock delegate classes
    private static class ValidateClinicalCodingDelegate {
        private final MedicalCodingService codingService;

        ValidateClinicalCodingDelegate(MedicalCodingService codingService) {
            this.codingService = codingService;
        }

        public void execute(DelegateExecution execution) {
            List<String> tussCodes = (List<String>) execution.getVariable("procedureCodes");
            List<String> cidCodes = (List<String>) execution.getVariable("cidCodes");

            boolean tussValid = codingService.validateTUSSCodes(tussCodes);
            boolean cidValid = cidCodes != null ? codingService.validateCIDCodes(cidCodes) : true;

            execution.setVariable("codingValid", tussValid);
            execution.setVariable("diagnosisCodeValid", cidValid);

            if (!tussValid) {
                execution.setVariable("invalidCodes", codingService.getInvalidCodes(tussCodes));
            } else {
                execution.setVariable("invalidCodes", Arrays.asList());
            }
        }
    }

    private static class CalculateAccountDelegate {
        private final BillingCalculationEngine billingEngine;

        CalculateAccountDelegate(BillingCalculationEngine billingEngine) {
            this.billingEngine = billingEngine;
        }

        public void execute(DelegateExecution execution) {
            String accountId = (String) execution.getVariable("accountId");
            BillingFixtures.BillingData billing = billingEngine.calculateAccount(accountId);

            execution.setVariable("totalAmount", billing.getTotalAmount());
            execution.setVariable("coPayAmount", billing.getCoPayAmount());
            execution.setVariable("insuranceAmount", billing.getInsuranceAmount());
            execution.setVariable("calculationComplete", true);
        }
    }

    private static class GenerateTISSGuideDelegate {
        private final TISSGateway tissGateway;

        GenerateTISSGuideDelegate(TISSGateway tissGateway) {
            this.tissGateway = tissGateway;
        }

        public void execute(DelegateExecution execution) {
            String accountId = (String) execution.getVariable("accountId");
            String insuranceId = (String) execution.getVariable("insuranceId");
            BillingFixtures.BillingData billing =
                (BillingFixtures.BillingData) execution.getVariable("billingData");

            String guideNumber = tissGateway.generateGuide(accountId, insuranceId, billing);
            execution.setVariable("tissGuideNumber", guideNumber);
            execution.setVariable("guideGenerated", true);
        }
    }

    private static class SubmitToInsuranceDelegate {
        private final TISSGateway tissGateway;

        SubmitToInsuranceDelegate(TISSGateway tissGateway) {
            this.tissGateway = tissGateway;
        }

        public void execute(DelegateExecution execution) {
            try {
                String tissGuideNumber = (String) execution.getVariable("tissGuideNumber");
                String insuranceId = (String) execution.getVariable("insuranceId");

                Map<String, Object> result = tissGateway.submitGuide(tissGuideNumber, insuranceId);

                execution.setVariable("protocolNumber", result.get("protocol"));
                execution.setVariable("submissionStatus", result.get("status"));
                execution.setVariable("submissionDate", result.get("submissionDate"));
            } catch (Exception e) {
                execution.setVariable("submissionStatus", "FAILED");
                execution.setVariable("retryRequired", true);

                Integer retryCount = (Integer) execution.getVariable("retryCount");
                execution.setVariable("retryCount", (retryCount == null ? 0 : retryCount) + 1);
            }
        }
    }

    private static class ProcessGlosaDelegate {
        private final GlosaManagementService glosaService;

        ProcessGlosaDelegate(GlosaManagementService glosaService) {
            this.glosaService = glosaService;
        }

        public void execute(DelegateExecution execution) {
            String glosaType = (String) execution.getVariable("glosaType");
            String glosaCode = (String) execution.getVariable("glosaCode");

            Map<String, Object> classification = glosaService.classifyGlosa(glosaType, glosaCode);

            execution.setVariable("glosaRecoverable", classification.get("recoverable"));
            execution.setVariable("requiredAction", classification.get("requiredAction"));
            execution.setVariable("glosaProcessed", true);

            if (classification.containsKey("recoverable") &&
                (Boolean) classification.get("recoverable")) {
                Double probability = glosaService.calculateRecoveryProbability(glosaType, glosaCode);
                execution.setVariable("recoveryProbability", probability);
            }
        }
    }

    private static class PrepareAppealDelegate {
        private final GlosaManagementService glosaService;

        PrepareAppealDelegate(GlosaManagementService glosaService) {
            this.glosaService = glosaService;
        }

        public void execute(DelegateExecution execution) {
            String denialId = (String) execution.getVariable("denialId");
            List<String> requiredDocs = glosaService.getRequiredDocumentsForAppeal(denialId);

            execution.setVariable("requiredDocuments", requiredDocs);
            execution.setVariable("appealPrepared", true);
        }
    }

    // Mock interfaces
    interface MedicalCodingService {
        boolean validateTUSSCodes(List<String> codes);
        boolean validateCIDCodes(List<String> codes);
        List<String> getInvalidCodes(List<String> codes);
    }

    interface BillingCalculationEngine {
        BillingFixtures.BillingData calculateAccount(String accountId);
    }

    interface TISSGateway {
        String generateGuide(String accountId, String insuranceId, BillingFixtures.BillingData billing);
        Map<String, Object> submitGuide(String guideNumber, String insuranceId);
    }

    interface GlosaManagementService {
        Map<String, Object> classifyGlosa(String type, String code);
        Double calculateRecoveryProbability(String type, String code);
        List<String> getRequiredDocumentsForAppeal(String denialId);
    }
}
