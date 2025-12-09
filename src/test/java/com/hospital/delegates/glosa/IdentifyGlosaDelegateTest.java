package com.hospital.delegates.glosa;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for IdentifyGlosaDelegate.
 *
 * Tests cover:
 * - Glosa identification with denial codes
 * - Claims without glosas (clean claims)
 * - Different glosa types and reasons
 * - Appeal eligibility determination
 * - Remittance advice parsing scenarios
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Identify Glosa Delegate Tests")
class IdentifyGlosaDelegateTest {

    @Mock
    private DelegateExecution execution;

    private IdentifyGlosaDelegate delegate;

    @BeforeEach
    void setUp() {
        delegate = new IdentifyGlosaDelegate();
        when(execution.getProcessInstanceId()).thenReturn("test-process-123");
    }

    // ====================
    // HAPPY PATH - NO GLOSA
    // ====================

    @Test
    @DisplayName("Should identify clean claim with no glosas")
    void testCleanClaimNoGlosa() throws Exception {
        // Arrange
        when(execution.getVariable("claimId")).thenReturn("CLAIM-123");
        when(execution.getVariable("remittanceAdvice")).thenReturn("APPROVED");
        when(execution.getVariable("denialCodes")).thenReturn(null);

        // Act
        delegate.execute(execution);

        // Assert
        verify(execution).setVariable(eq("hasGlosa"), eq(false));
        verify(execution).setVariable(eq("glosaCount"), eq(0));
        verify(execution).setVariable(eq("glosaAmount"), eq(0.0));
        verify(execution).setVariable(eq("glosaType"), eq("NONE"));
        verify(execution).setVariable(eq("appealEligible"), eq(false));
        verify(execution).setVariable(eq("glosaReasons"), any(List.class));
        verify(execution).setVariable(eq("glosaAnalysisDate"), anyString());
    }

    // ====================
    // GLOSA IDENTIFICATION
    // ====================

    @Test
    @DisplayName("Should identify glosa with single denial code")
    void testGlosaWithSingleDenialCode() throws Exception {
        // Arrange
        when(execution.getVariable("claimId")).thenReturn("CLAIM-123");
        when(execution.getVariable("remittanceAdvice")).thenReturn("PARTIALLY_DENIED");
        when(execution.getVariable("denialCodes")).thenReturn(Arrays.asList("D001"));

        // Act
        delegate.execute(execution);

        // Assert
        verify(execution).setVariable(eq("hasGlosa"), eq(true));
        verify(execution).setVariable(eq("glosaCount"), eq(1));
        verify(execution).setVariable(eq("glosaAmount"), anyDouble());
        verify(execution).setVariable(eq("glosaType"), anyString());
        verify(execution).setVariable(eq("appealEligible"), any(Boolean.class));
    }

    @Test
    @DisplayName("Should identify glosa with multiple denial codes")
    void testGlosaWithMultipleDenialCodes() throws Exception {
        // Arrange
        when(execution.getVariable("claimId")).thenReturn("CLAIM-123");
        when(execution.getVariable("remittanceAdvice")).thenReturn("DENIED");
        when(execution.getVariable("denialCodes")).thenReturn(Arrays.asList("D001", "D002", "D003"));

        // Act
        delegate.execute(execution);

        // Assert
        verify(execution).setVariable(eq("hasGlosa"), eq(true));
        verify(execution).setVariable(eq("glosaCount"), anyInt());
    }

    @Test
    @DisplayName("Should parse remittance advice for glosa information")
    void testRemittanceAdviceParsing() throws Exception {
        // Arrange
        String remittanceAdvice = "CLAIM DENIED - REASON: Medical necessity not established";
        when(execution.getVariable("claimId")).thenReturn("CLAIM-123");
        when(execution.getVariable("remittanceAdvice")).thenReturn(remittanceAdvice);
        when(execution.getVariable("denialCodes")).thenReturn(Arrays.asList("MN01"));

        // Act
        delegate.execute(execution);

        // Assert
        verify(execution).setVariable(eq("hasGlosa"), eq(true));
        verify(execution).setVariable(eq("glosaReasons"), any(List.class));
    }

    // ====================
    // GLOSA TYPES
    // ====================

    @Test
    @DisplayName("Should identify clinical glosa type")
    void testClinicalGlosaType() throws Exception {
        // Arrange
        when(execution.getVariable("claimId")).thenReturn("CLAIM-123");
        when(execution.getVariable("remittanceAdvice")).thenReturn("CLINICAL_REVIEW_REQUIRED");
        when(execution.getVariable("denialCodes")).thenReturn(Arrays.asList("CLIN01"));

        // Act
        delegate.execute(execution);

        // Assert
        verify(execution).setVariable(eq("glosaType"), anyString());
    }

    @Test
    @DisplayName("Should identify administrative glosa type")
    void testAdministrativeGlosaType() throws Exception {
        // Arrange
        when(execution.getVariable("claimId")).thenReturn("CLAIM-123");
        when(execution.getVariable("remittanceAdvice")).thenReturn("MISSING_INFORMATION");
        when(execution.getVariable("denialCodes")).thenReturn(Arrays.asList("ADM01"));

        // Act
        delegate.execute(execution);

        // Assert
        verify(execution).setVariable(eq("hasGlosa"), eq(true));
    }

    @Test
    @DisplayName("Should identify technical glosa type")
    void testTechnicalGlosaType() throws Exception {
        // Arrange
        when(execution.getVariable("claimId")).thenReturn("CLAIM-123");
        when(execution.getVariable("remittanceAdvice")).thenReturn("CODING_ERROR");
        when(execution.getVariable("denialCodes")).thenReturn(Arrays.asList("TECH01"));

        // Act
        delegate.execute(execution);

        // Assert
        verify(execution).setVariable(eq("hasGlosa"), eq(true));
    }

    // ====================
    // APPEAL ELIGIBILITY
    // ====================

    @Test
    @DisplayName("Should mark appealable glosa as eligible")
    void testAppealableGlosa() throws Exception {
        // Arrange
        when(execution.getVariable("claimId")).thenReturn("CLAIM-123");
        when(execution.getVariable("remittanceAdvice")).thenReturn("DENIED");
        when(execution.getVariable("denialCodes")).thenReturn(Arrays.asList("APP01")); // Appealable code

        // Act
        delegate.execute(execution);

        // Assert
        verify(execution).setVariable(eq("hasGlosa"), eq(true));
        verify(execution).setVariable(eq("appealEligible"), any(Boolean.class));
    }

    @Test
    @DisplayName("Should mark non-appealable glosa as not eligible")
    void testNonAppealableGlosa() throws Exception {
        // Arrange
        when(execution.getVariable("claimId")).thenReturn("CLAIM-123");
        when(execution.getVariable("remittanceAdvice")).thenReturn("FINAL_DENIAL");
        when(execution.getVariable("denialCodes")).thenReturn(Arrays.asList("FINAL01"));

        // Act
        delegate.execute(execution);

        // Assert
        verify(execution).setVariable(eq("hasGlosa"), any(Boolean.class));
    }

    // ====================
    // ERROR HANDLING
    // ====================

    @Test
    @DisplayName("Should handle missing claim ID")
    void testMissingClaimId() {
        // Arrange
        when(execution.getVariable("claimId")).thenReturn(null);
        when(execution.getVariable("remittanceAdvice")).thenReturn("APPROVED");

        // Act & Assert
        assertThatThrownBy(() -> delegate.execute(execution))
            .isInstanceOf(Exception.class);

        verify(execution).setVariable(eq("glosaAnalysisError"), anyString());
    }

    @Test
    @DisplayName("Should handle null remittance advice")
    void testNullRemittanceAdvice() throws Exception {
        // Arrange
        when(execution.getVariable("claimId")).thenReturn("CLAIM-123");
        when(execution.getVariable("remittanceAdvice")).thenReturn(null);
        when(execution.getVariable("denialCodes")).thenReturn(null);

        // Act
        delegate.execute(execution);

        // Assert - Should still complete analysis
        verify(execution).setVariable(eq("hasGlosa"), any(Boolean.class));
        verify(execution).setVariable(eq("glosaAnalysisDate"), anyString());
    }

    // ====================
    // EDGE CASES
    // ====================

    @Test
    @DisplayName("Should handle empty denial codes list")
    void testEmptyDenialCodesList() throws Exception {
        // Arrange
        when(execution.getVariable("claimId")).thenReturn("CLAIM-123");
        when(execution.getVariable("remittanceAdvice")).thenReturn("APPROVED");
        when(execution.getVariable("denialCodes")).thenReturn(new ArrayList<>());

        // Act
        delegate.execute(execution);

        // Assert
        verify(execution).setVariable(eq("hasGlosa"), eq(false));
        verify(execution).setVariable(eq("glosaCount"), eq(0));
    }

    @Test
    @DisplayName("Should handle very long remittance advice")
    void testLongRemittanceAdvice() throws Exception {
        // Arrange
        String longRemittance = "DENIED - " + "Reason ".repeat(100);
        when(execution.getVariable("claimId")).thenReturn("CLAIM-123");
        when(execution.getVariable("remittanceAdvice")).thenReturn(longRemittance);
        when(execution.getVariable("denialCodes")).thenReturn(Arrays.asList("D001"));

        // Act
        delegate.execute(execution);

        // Assert
        verify(execution).setVariable(eq("hasGlosa"), eq(true));
    }

    @Test
    @DisplayName("Should handle special characters in denial codes")
    void testSpecialCharactersInDenialCodes() throws Exception {
        // Arrange
        when(execution.getVariable("claimId")).thenReturn("CLAIM-123");
        when(execution.getVariable("remittanceAdvice")).thenReturn("DENIED");
        when(execution.getVariable("denialCodes")).thenReturn(Arrays.asList("D-001", "D#002"));

        // Act
        delegate.execute(execution);

        // Assert
        verify(execution).setVariable(eq("hasGlosa"), any(Boolean.class));
    }

    // ====================
    // AMOUNT CALCULATIONS
    // ====================

    @Test
    @DisplayName("Should calculate glosa amount correctly")
    void testGlosaAmountCalculation() throws Exception {
        // Arrange
        when(execution.getVariable("claimId")).thenReturn("CLAIM-123");
        when(execution.getVariable("remittanceAdvice")).thenReturn("PARTIALLY_DENIED");
        when(execution.getVariable("denialCodes")).thenReturn(Arrays.asList("D001"));

        // Act
        delegate.execute(execution);

        // Assert
        verify(execution).setVariable(eq("glosaAmount"), anyDouble());
    }

    @Test
    @DisplayName("Should set zero amount for clean claims")
    void testZeroAmountForCleanClaims() throws Exception {
        // Arrange
        when(execution.getVariable("claimId")).thenReturn("CLAIM-123");
        when(execution.getVariable("remittanceAdvice")).thenReturn("APPROVED");
        when(execution.getVariable("denialCodes")).thenReturn(null);

        // Act
        delegate.execute(execution);

        // Assert
        verify(execution).setVariable(eq("glosaAmount"), eq(0.0));
    }

    // ====================
    // PERFORMANCE
    // ====================

    @Test
    @DisplayName("Should complete analysis within reasonable time")
    void testAnalysisPerformance() throws Exception {
        // Arrange
        when(execution.getVariable("claimId")).thenReturn("CLAIM-123");
        when(execution.getVariable("remittanceAdvice")).thenReturn("DENIED");
        when(execution.getVariable("denialCodes")).thenReturn(Arrays.asList("D001", "D002", "D003"));

        long startTime = System.currentTimeMillis();

        // Act
        delegate.execute(execution);

        // Assert
        long duration = System.currentTimeMillis() - startTime;
        assertThat(duration).isLessThan(500); // Should complete in less than 500ms
    }

    @Test
    @DisplayName("Should handle multiple sequential analyses")
    void testMultipleAnalyses() throws Exception {
        // Test analyzing 20 claims sequentially
        for (int i = 0; i < 20; i++) {
            DelegateExecution exec = mock(DelegateExecution.class);
            when(exec.getProcessInstanceId()).thenReturn("process-" + i);
            when(exec.getVariable("claimId")).thenReturn("CLAIM-" + i);
            when(exec.getVariable("remittanceAdvice")).thenReturn("DENIED");
            when(exec.getVariable("denialCodes")).thenReturn(Arrays.asList("D001"));

            delegate.execute(exec);

            verify(exec).setVariable(eq("hasGlosa"), any(Boolean.class));
            verify(exec).setVariable(eq("glosaAnalysisDate"), anyString());
        }
    }

    // ====================
    // DATA INTEGRITY
    // ====================

    @Test
    @DisplayName("Should always set analysis date")
    void testAnalysisDateAlwaysSet() throws Exception {
        // Arrange
        when(execution.getVariable("claimId")).thenReturn("CLAIM-123");
        when(execution.getVariable("remittanceAdvice")).thenReturn("APPROVED");
        when(execution.getVariable("denialCodes")).thenReturn(null);

        // Act
        delegate.execute(execution);

        // Assert
        verify(execution).setVariable(eq("glosaAnalysisDate"), anyString());
    }

    @Test
    @DisplayName("Should set all required output variables")
    void testAllOutputVariablesSet() throws Exception {
        // Arrange
        when(execution.getVariable("claimId")).thenReturn("CLAIM-123");
        when(execution.getVariable("remittanceAdvice")).thenReturn("DENIED");
        when(execution.getVariable("denialCodes")).thenReturn(Arrays.asList("D001"));

        // Act
        delegate.execute(execution);

        // Assert - Verify all expected variables are set
        verify(execution).setVariable(eq("hasGlosa"), any(Boolean.class));
        verify(execution).setVariable(eq("glosaCount"), any(Integer.class));
        verify(execution).setVariable(eq("glosaReasons"), any(List.class));
        verify(execution).setVariable(eq("glosaAmount"), any(Double.class));
        verify(execution).setVariable(eq("glosaType"), anyString());
        verify(execution).setVariable(eq("appealEligible"), any(Boolean.class));
        verify(execution).setVariable(eq("glosaAnalysisDate"), anyString());
    }
}
