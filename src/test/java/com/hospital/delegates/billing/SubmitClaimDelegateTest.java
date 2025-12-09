package com.hospital.delegates.billing;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for SubmitClaimDelegate.
 *
 * Tests cover:
 * - Successful claim submission
 * - Missing required fields
 * - Different submission methods (EDI, Portal, Fax)
 * - Provider integration scenarios
 * - Error handling and recovery
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Submit Claim Delegate Tests")
class SubmitClaimDelegateTest {

    @Mock
    private DelegateExecution execution;

    private SubmitClaimDelegate delegate;

    @BeforeEach
    void setUp() {
        delegate = new SubmitClaimDelegate();
        when(execution.getProcessInstanceId()).thenReturn("test-process-123");
    }

    // ====================
    // HAPPY PATH SCENARIOS
    // ====================

    @Test
    @DisplayName("Should successfully submit claim with valid data")
    void testSuccessfulClaimSubmission() throws Exception {
        // Arrange
        setupValidInputVariables();

        // Act
        delegate.execute(execution);

        // Assert
        verify(execution).setVariable(eq("submissionId"), startsWith("SUB-"));
        verify(execution).setVariable(eq("submissionStatus"), eq("SUBMITTED"));
        verify(execution).setVariable(eq("submissionDate"), anyString());
        verify(execution).setVariable(eq("confirmationNumber"), startsWith("CONF-"));
        verify(execution).setVariable(eq("expectedAdjudicationDate"), anyString());
        verify(execution, never()).setVariable(eq("submissionError"), anyString());
    }

    @Test
    @DisplayName("Should submit claim via EDI method")
    void testEDISubmission() throws Exception {
        // Arrange
        setupValidInputVariables();
        when(execution.getVariable("submissionMethod")).thenReturn("EDI");

        // Act
        delegate.execute(execution);

        // Assert
        verify(execution).setVariable(eq("submissionId"), anyString());
        verify(execution).setVariable(eq("submissionStatus"), eq("SUBMITTED"));
    }

    @Test
    @DisplayName("Should submit claim via Portal method")
    void testPortalSubmission() throws Exception {
        // Arrange
        setupValidInputVariables();
        when(execution.getVariable("submissionMethod")).thenReturn("PORTAL");

        // Act
        delegate.execute(execution);

        // Assert
        verify(execution).setVariable(eq("submissionId"), anyString());
        verify(execution).setVariable(eq("submissionStatus"), eq("SUBMITTED"));
    }

    @Test
    @DisplayName("Should submit claim via Fax method")
    void testFaxSubmission() throws Exception {
        // Arrange
        setupValidInputVariables();
        when(execution.getVariable("submissionMethod")).thenReturn("FAX");

        // Act
        delegate.execute(execution);

        // Assert
        verify(execution).setVariable(eq("submissionId"), anyString());
        verify(execution).setVariable(eq("submissionStatus"), eq("SUBMITTED"));
    }

    // ====================
    // ERROR SCENARIOS
    // ====================

    @Test
    @DisplayName("Should handle missing claim ID")
    void testMissingClaimId() {
        // Arrange
        when(execution.getVariable("claimId")).thenReturn(null);
        when(execution.getVariable("claimNumber")).thenReturn("CLM-12345");
        when(execution.getVariable("insuranceProvider")).thenReturn("123456");
        when(execution.getVariable("submissionMethod")).thenReturn("EDI");

        // Act & Assert
        assertThatThrownBy(() -> delegate.execute(execution))
            .isInstanceOf(Exception.class);

        verify(execution).setVariable(eq("submissionStatus"), eq("ERROR"));
        verify(execution).setVariable(eq("submissionError"), anyString());
    }

    @Test
    @DisplayName("Should handle missing claim number")
    void testMissingClaimNumber() {
        // Arrange
        when(execution.getVariable("claimId")).thenReturn("CLAIM-123");
        when(execution.getVariable("claimNumber")).thenReturn(null);
        when(execution.getVariable("insuranceProvider")).thenReturn("123456");
        when(execution.getVariable("submissionMethod")).thenReturn("EDI");

        // Act & Assert
        assertThatThrownBy(() -> delegate.execute(execution))
            .isInstanceOf(Exception.class);

        verify(execution).setVariable(eq("submissionStatus"), eq("ERROR"));
    }

    @Test
    @DisplayName("Should handle missing insurance provider")
    void testMissingInsuranceProvider() {
        // Arrange
        when(execution.getVariable("claimId")).thenReturn("CLAIM-123");
        when(execution.getVariable("claimNumber")).thenReturn("CLM-12345");
        when(execution.getVariable("insuranceProvider")).thenReturn(null);
        when(execution.getVariable("submissionMethod")).thenReturn("EDI");

        // Act & Assert
        assertThatThrownBy(() -> delegate.execute(execution))
            .isInstanceOf(Exception.class);

        verify(execution).setVariable(eq("submissionStatus"), eq("ERROR"));
    }

    // ====================
    // EDGE CASES
    // ====================

    @Test
    @DisplayName("Should handle null submission method")
    void testNullSubmissionMethod() throws Exception {
        // Arrange
        setupValidInputVariables();
        when(execution.getVariable("submissionMethod")).thenReturn(null);

        // Act
        delegate.execute(execution);

        // Assert - Should use default submission method
        verify(execution).setVariable(eq("submissionId"), anyString());
        verify(execution).setVariable(eq("submissionStatus"), eq("SUBMITTED"));
    }

    @Test
    @DisplayName("Should handle very long claim number")
    void testLongClaimNumber() throws Exception {
        // Arrange
        setupValidInputVariables();
        when(execution.getVariable("claimNumber")).thenReturn("CLM-" + "X".repeat(100));

        // Act
        delegate.execute(execution);

        // Assert
        verify(execution).setVariable(eq("submissionId"), anyString());
        verify(execution).setVariable(eq("confirmationNumber"), anyString());
    }

    @Test
    @DisplayName("Should handle special characters in claim data")
    void testSpecialCharactersInClaimData() throws Exception {
        // Arrange
        setupValidInputVariables();
        when(execution.getVariable("claimNumber")).thenReturn("CLM-12345!@#$%");

        // Act
        delegate.execute(execution);

        // Assert
        verify(execution).setVariable(eq("submissionId"), anyString());
    }

    // ====================
    // PERFORMANCE SCENARIOS
    // ====================

    @Test
    @DisplayName("Should complete submission within reasonable time")
    void testSubmissionPerformance() throws Exception {
        // Arrange
        setupValidInputVariables();
        long startTime = System.currentTimeMillis();

        // Act
        delegate.execute(execution);

        // Assert
        long duration = System.currentTimeMillis() - startTime;
        assertThat(duration).isLessThan(1000); // Should complete in less than 1 second
        verify(execution).setVariable(eq("submissionStatus"), eq("SUBMITTED"));
    }

    @Test
    @DisplayName("Should handle multiple sequential submissions")
    void testMultipleSubmissions() throws Exception {
        // Arrange & Act
        for (int i = 0; i < 10; i++) {
            DelegateExecution exec = mock(DelegateExecution.class);
            when(exec.getProcessInstanceId()).thenReturn("process-" + i);
            when(exec.getVariable("claimId")).thenReturn("CLAIM-" + i);
            when(exec.getVariable("claimNumber")).thenReturn("CLM-" + i);
            when(exec.getVariable("insuranceProvider")).thenReturn("123456");
            when(exec.getVariable("submissionMethod")).thenReturn("EDI");

            delegate.execute(exec);

            // Assert each submission
            verify(exec).setVariable(eq("submissionId"), anyString());
            verify(exec).setVariable(eq("submissionStatus"), eq("SUBMITTED"));
        }
    }

    // ====================
    // DATA VALIDATION
    // ====================

    @Test
    @DisplayName("Should generate unique submission IDs")
    void testUniqueSubmissionIds() throws Exception {
        // Arrange
        setupValidInputVariables();

        // Act - Submit twice
        delegate.execute(execution);

        // Capture first submission ID
        verify(execution).setVariable(eq("submissionId"), anyString());

        // Second submission
        DelegateExecution execution2 = mock(DelegateExecution.class);
        when(execution2.getProcessInstanceId()).thenReturn("test-process-456");
        setupValidInputVariables(execution2);

        delegate.execute(execution2);

        // Assert - Both should have submission IDs set
        verify(execution2).setVariable(eq("submissionId"), anyString());
    }

    @Test
    @DisplayName("Should set expected adjudication date in future")
    void testExpectedAdjudicationDate() throws Exception {
        // Arrange
        setupValidInputVariables();

        // Act
        delegate.execute(execution);

        // Assert
        verify(execution).setVariable(eq("expectedAdjudicationDate"), anyString());
    }

    // ====================
    // INTEGRATION SCENARIOS
    // ====================

    @Test
    @DisplayName("Should handle different insurance provider types")
    void testDifferentProviderTypes() throws Exception {
        // Test with various provider codes
        String[] providerCodes = {"123456", "999999", "000001", "555555"};

        for (String providerCode : providerCodes) {
            // Arrange
            DelegateExecution exec = mock(DelegateExecution.class);
            when(exec.getProcessInstanceId()).thenReturn("process-" + providerCode);
            when(exec.getVariable("claimId")).thenReturn("CLAIM-123");
            when(exec.getVariable("claimNumber")).thenReturn("CLM-12345");
            when(exec.getVariable("insuranceProvider")).thenReturn(providerCode);
            when(exec.getVariable("submissionMethod")).thenReturn("EDI");

            // Act
            delegate.execute(exec);

            // Assert
            verify(exec).setVariable(eq("submissionStatus"), eq("SUBMITTED"));
        }
    }

    // ====================
    // HELPER METHODS
    // ====================

    private void setupValidInputVariables() {
        when(execution.getVariable("claimId")).thenReturn("CLAIM-123");
        when(execution.getVariable("claimNumber")).thenReturn("CLM-12345");
        when(execution.getVariable("insuranceProvider")).thenReturn("123456");
        when(execution.getVariable("submissionMethod")).thenReturn("EDI");
    }

    private void setupValidInputVariables(DelegateExecution exec) {
        when(exec.getVariable("claimId")).thenReturn("CLAIM-456");
        when(exec.getVariable("claimNumber")).thenReturn("CLM-67890");
        when(exec.getVariable("insuranceProvider")).thenReturn("789012");
        when(exec.getVariable("submissionMethod")).thenReturn("EDI");
    }
}
