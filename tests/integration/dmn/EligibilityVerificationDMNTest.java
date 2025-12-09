package com.hospital.integration.dmn;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.InputStream;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for eligibility-verification.dmn decision table.
 * Validates insurance eligibility rules and waiting period calculations.
 */
@DisplayName("Eligibility Verification DMN Tests")
class EligibilityVerificationDMNTest {

    private static DmnEngine dmnEngine;
    private static DmnDecision decision;

    @BeforeAll
    static void setUp() {
        // Initialize DMN engine
        dmnEngine = DmnEngineConfiguration
            .createDefaultDmnEngineConfiguration()
            .buildEngine();

        // Load DMN decision table
        InputStream inputStream = EligibilityVerificationDMNTest.class
            .getResourceAsStream("/dmn/eligibility-verification.dmn");

        decision = dmnEngine.parseDecision("EligibilityDecision", inputStream);
    }

    @Test
    @DisplayName("Should approve eligibility for plan with no waiting period")
    void shouldApproveEligibilityForPlanWithNoWaitingPeriod() {
        // Arrange
        VariableMap variables = Variables.createVariables()
            .putValue("planType", "PREMIUM")
            .putValue("enrollmentDate", LocalDate.now().minusMonths(1))
            .putValue("procedureType", "CONSULTATION")
            .putValue("planActive", true);

        // Act
        DmnDecisionTableResult result = dmnEngine.evaluateDecisionTable(decision, variables);

        // Assert
        assertThat(result.getSingleResult()).isNotNull();
        assertThat(result.getSingleResult().get("eligible")).isEqualTo(true);
        assertThat(result.getSingleResult().get("reason")).isEqualTo("ELIGIBLE");
    }

    @ParameterizedTest
    @CsvSource({
        "BASIC, SURGERY, 180, false, WAITING_PERIOD",
        "STANDARD, SURGERY, 90, false, WAITING_PERIOD",
        "PREMIUM, SURGERY, 30, false, WAITING_PERIOD",
        "BASIC, CHILDBIRTH, 300, false, WAITING_PERIOD"
    })
    @DisplayName("Should deny eligibility during waiting period")
    void shouldDenyEligibilityDuringWaitingPeriod(
        String planType,
        String procedureType,
        int waitingDays,
        boolean expectedEligible,
        String expectedReason
    ) {
        // Arrange
        VariableMap variables = Variables.createVariables()
            .putValue("planType", planType)
            .putValue("enrollmentDate", LocalDate.now().minusDays(waitingDays - 1))
            .putValue("procedureType", procedureType)
            .putValue("planActive", true);

        // Act
        DmnDecisionTableResult result = dmnEngine.evaluateDecisionTable(decision, variables);

        // Assert
        assertThat(result.getSingleResult().get("eligible")).isEqualTo(expectedEligible);
        assertThat(result.getSingleResult().get("reason")).isEqualTo(expectedReason);
    }

    @ParameterizedTest
    @CsvSource({
        "BASIC, SURGERY, 181, true, ELIGIBLE",
        "STANDARD, SURGERY, 91, true, ELIGIBLE",
        "PREMIUM, SURGERY, 31, true, ELIGIBLE",
        "BASIC, CHILDBIRTH, 301, true, ELIGIBLE"
    })
    @DisplayName("Should approve eligibility after waiting period")
    void shouldApproveEligibilityAfterWaitingPeriod(
        String planType,
        String procedureType,
        int daysSinceEnrollment,
        boolean expectedEligible,
        String expectedReason
    ) {
        // Arrange
        VariableMap variables = Variables.createVariables()
            .putValue("planType", planType)
            .putValue("enrollmentDate", LocalDate.now().minusDays(daysSinceEnrollment))
            .putValue("procedureType", procedureType)
            .putValue("planActive", true);

        // Act
        DmnDecisionTableResult result = dmnEngine.evaluateDecisionTable(decision, variables);

        // Assert
        assertThat(result.getSingleResult().get("eligible")).isEqualTo(expectedEligible);
        assertThat(result.getSingleResult().get("reason")).isEqualTo(expectedReason);
    }

    @Test
    @DisplayName("Should deny eligibility for inactive plan")
    void shouldDenyEligibilityForInactivePlan() {
        // Arrange
        VariableMap variables = Variables.createVariables()
            .putValue("planType", "PREMIUM")
            .putValue("enrollmentDate", LocalDate.now().minusYears(1))
            .putValue("procedureType", "CONSULTATION")
            .putValue("planActive", false);

        // Act
        DmnDecisionTableResult result = dmnEngine.evaluateDecisionTable(decision, variables);

        // Assert
        assertThat(result.getSingleResult().get("eligible")).isEqualTo(false);
        assertThat(result.getSingleResult().get("reason")).isEqualTo("INACTIVE_PLAN");
    }

    @Test
    @DisplayName("Should approve emergency procedures regardless of waiting period")
    void shouldApproveEmergencyProceduresRegardlessOfWaitingPeriod() {
        // Arrange
        VariableMap variables = Variables.createVariables()
            .putValue("planType", "BASIC")
            .putValue("enrollmentDate", LocalDate.now().minusDays(1))
            .putValue("procedureType", "EMERGENCY")
            .putValue("planActive", true);

        // Act
        DmnDecisionTableResult result = dmnEngine.evaluateDecisionTable(decision, variables);

        // Assert
        assertThat(result.getSingleResult().get("eligible")).isEqualTo(true);
        assertThat(result.getSingleResult().get("reason")).isEqualTo("EMERGENCY_COVERAGE");
    }

    @Test
    @DisplayName("Should calculate remaining days in waiting period")
    void shouldCalculateRemainingDaysInWaitingPeriod() {
        // Arrange
        int daysEnrolled = 100;
        int waitingPeriodDays = 180;

        VariableMap variables = Variables.createVariables()
            .putValue("planType", "BASIC")
            .putValue("enrollmentDate", LocalDate.now().minusDays(daysEnrolled))
            .putValue("procedureType", "SURGERY")
            .putValue("planActive", true);

        // Act
        DmnDecisionTableResult result = dmnEngine.evaluateDecisionTable(decision, variables);

        // Assert
        int expectedRemainingDays = waitingPeriodDays - daysEnrolled;
        assertThat(result.getSingleResult().get("remainingDays"))
            .isEqualTo(expectedRemainingDays);
    }

    @ParameterizedTest
    @CsvSource({
        "PREMIUM, CONSULTATION, 100",
        "PREMIUM, EXAM, 80",
        "STANDARD, CONSULTATION, 90",
        "STANDARD, SURGERY, 70",
        "BASIC, CONSULTATION, 80",
        "BASIC, SURGERY, 50"
    })
    @DisplayName("Should calculate coverage percentage based on plan and procedure")
    void shouldCalculateCoveragePercentageBasedOnPlanAndProcedure(
        String planType,
        String procedureType,
        int expectedCoverage
    ) {
        // Arrange
        VariableMap variables = Variables.createVariables()
            .putValue("planType", planType)
            .putValue("enrollmentDate", LocalDate.now().minusYears(1))
            .putValue("procedureType", procedureType)
            .putValue("planActive", true);

        // Act
        DmnDecisionTableResult result = dmnEngine.evaluateDecisionTable(decision, variables);

        // Assert
        assertThat(result.getSingleResult().get("coveragePercent"))
            .isEqualTo(expectedCoverage);
    }

    @Test
    @DisplayName("Should approve for maternity after 300-day waiting period")
    void shouldApproveForMaternityAfter300DayWaitingPeriod() {
        // Arrange
        VariableMap variables = Variables.createVariables()
            .putValue("planType", "BASIC")
            .putValue("enrollmentDate", LocalDate.now().minusDays(301))
            .putValue("procedureType", "CHILDBIRTH")
            .putValue("planActive", true);

        // Act
        DmnDecisionTableResult result = dmnEngine.evaluateDecisionTable(decision, variables);

        // Assert
        assertThat(result.getSingleResult().get("eligible")).isEqualTo(true);
        assertThat(result.getSingleResult().get("reason")).isEqualTo("ELIGIBLE");
    }

    @Test
    @DisplayName("Should handle pre-existing condition exclusion")
    void shouldHandlePreExistingConditionExclusion() {
        // Arrange
        VariableMap variables = Variables.createVariables()
            .putValue("planType", "BASIC")
            .putValue("enrollmentDate", LocalDate.now().minusDays(100))
            .putValue("procedureType", "SURGERY")
            .putValue("planActive", true)
            .putValue("preExistingCondition", true)
            .putValue("preExistingWaitingPeriod", 720); // 24 months

        // Act
        DmnDecisionTableResult result = dmnEngine.evaluateDecisionTable(decision, variables);

        // Assert
        assertThat(result.getSingleResult().get("eligible")).isEqualTo(false);
        assertThat(result.getSingleResult().get("reason"))
            .isEqualTo("PRE_EXISTING_WAITING_PERIOD");
    }

    @Test
    @DisplayName("Should require authorization for high-cost procedures")
    void shouldRequireAuthorizationForHighCostProcedures() {
        // Arrange
        VariableMap variables = Variables.createVariables()
            .putValue("planType", "PREMIUM")
            .putValue("enrollmentDate", LocalDate.now().minusYears(1))
            .putValue("procedureType", "SURGERY")
            .putValue("planActive", true)
            .putValue("estimatedCost", 50000.0);

        // Act
        DmnDecisionTableResult result = dmnEngine.evaluateDecisionTable(decision, variables);

        // Assert
        assertThat(result.getSingleResult().get("requiresAuthorization")).isEqualTo(true);
        assertThat(result.getSingleResult().get("authorizationType"))
            .isEqualTo("PRIOR_AUTHORIZATION");
    }

    @Test
    @DisplayName("Should handle multiple rules matching for complex scenario")
    void shouldHandleMultipleRulesMatchingForComplexScenario() {
        // Arrange
        VariableMap variables = Variables.createVariables()
            .putValue("planType", "PREMIUM")
            .putValue("enrollmentDate", LocalDate.now().minusMonths(6))
            .putValue("procedureType", "ICU")
            .putValue("planActive", true)
            .putValue("estimatedCost", 100000.0)
            .putValue("urgencyLevel", "HIGH");

        // Act
        DmnDecisionTableResult result = dmnEngine.evaluateDecisionTable(decision, variables);

        // Assert - Should get first matching rule result
        assertThat(result.getResultList()).isNotEmpty();
        assertThat(result.getSingleResult()).isNotNull();
    }
}
