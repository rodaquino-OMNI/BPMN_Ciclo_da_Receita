package com.hospital.fixtures;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Test fixtures for Insurance and Authorization entities.
 */
public class InsuranceFixtures {

    public static class Insurance {
        private String id;
        private String name;
        private String planCode;
        private String planType;
        private BigDecimal coPayPercent;
        private BigDecimal coPayFixed;
        private boolean requiresAuthorization;
        private String portalUrl;
        private String webserviceUrl;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPlanCode() { return planCode; }
        public void setPlanCode(String planCode) { this.planCode = planCode; }
        public String getPlanType() { return planType; }
        public void setPlanType(String planType) { this.planType = planType; }
        public BigDecimal getCoPayPercent() { return coPayPercent; }
        public void setCoPayPercent(BigDecimal coPayPercent) { this.coPayPercent = coPayPercent; }
        public BigDecimal getCoPayFixed() { return coPayFixed; }
        public void setCoPayFixed(BigDecimal coPayFixed) { this.coPayFixed = coPayFixed; }
        public boolean isRequiresAuthorization() { return requiresAuthorization; }
        public void setRequiresAuthorization(boolean requiresAuthorization) { this.requiresAuthorization = requiresAuthorization; }
        public String getPortalUrl() { return portalUrl; }
        public void setPortalUrl(String portalUrl) { this.portalUrl = portalUrl; }
        public String getWebserviceUrl() { return webserviceUrl; }
        public void setWebserviceUrl(String webserviceUrl) { this.webserviceUrl = webserviceUrl; }
    }

    public static class AuthorizationRequest {
        private String authorizationNumber;
        private String patientId;
        private String insuranceId;
        private List<String> procedureCodes;
        private List<String> cidCodes;
        private LocalDate requestDate;
        private LocalDate procedureDate;
        private String requestingPhysician;
        private String status;
        private String denialReason;
        private BigDecimal estimatedCost;

        // Getters and setters
        public String getAuthorizationNumber() { return authorizationNumber; }
        public void setAuthorizationNumber(String authorizationNumber) { this.authorizationNumber = authorizationNumber; }
        public String getPatientId() { return patientId; }
        public void setPatientId(String patientId) { this.patientId = patientId; }
        public String getInsuranceId() { return insuranceId; }
        public void setInsuranceId(String insuranceId) { this.insuranceId = insuranceId; }
        public List<String> getProcedureCodes() { return procedureCodes; }
        public void setProcedureCodes(List<String> procedureCodes) { this.procedureCodes = procedureCodes; }
        public List<String> getCidCodes() { return cidCodes; }
        public void setCidCodes(List<String> cidCodes) { this.cidCodes = cidCodes; }
        public LocalDate getRequestDate() { return requestDate; }
        public void setRequestDate(LocalDate requestDate) { this.requestDate = requestDate; }
        public LocalDate getProcedureDate() { return procedureDate; }
        public void setProcedureDate(LocalDate procedureDate) { this.procedureDate = procedureDate; }
        public String getRequestingPhysician() { return requestingPhysician; }
        public void setRequestingPhysician(String requestingPhysician) { this.requestingPhysician = requestingPhysician; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getDenialReason() { return denialReason; }
        public void setDenialReason(String denialReason) { this.denialReason = denialReason; }
        public BigDecimal getEstimatedCost() { return estimatedCost; }
        public void setEstimatedCost(BigDecimal estimatedCost) { this.estimatedCost = estimatedCost; }
    }

    /**
     * Unimed insurance with typical configuration
     */
    public static Insurance unimed() {
        Insurance insurance = new Insurance();
        insurance.setId("UNIMED");
        insurance.setName("Unimed São Paulo");
        insurance.setPlanCode("UNIMED_PREMIUM_001");
        insurance.setPlanType("PREMIUM");
        insurance.setCoPayPercent(new BigDecimal("20.00"));
        insurance.setCoPayFixed(BigDecimal.ZERO);
        insurance.setRequiresAuthorization(true);
        insurance.setPortalUrl("https://portal.unimed.com.br");
        insurance.setWebserviceUrl("https://ws.unimed.com.br/v1");
        return insurance;
    }

    /**
     * Bradesco insurance
     */
    public static Insurance bradesco() {
        Insurance insurance = new Insurance();
        insurance.setId("BRADESCO");
        insurance.setName("Bradesco Saúde");
        insurance.setPlanCode("BRADESCO_TOP_001");
        insurance.setPlanType("TOP");
        insurance.setCoPayPercent(new BigDecimal("15.00"));
        insurance.setCoPayFixed(new BigDecimal("50.00"));
        insurance.setRequiresAuthorization(true);
        insurance.setPortalUrl("https://portal.bradescosaude.com.br");
        insurance.setWebserviceUrl("https://api.bradescosaude.com.br");
        return insurance;
    }

    /**
     * Amil insurance
     */
    public static Insurance amil() {
        Insurance insurance = new Insurance();
        insurance.setId("AMIL");
        insurance.setName("Amil Saúde");
        insurance.setPlanCode("AMIL_S750_001");
        insurance.setPlanType("S750");
        insurance.setCoPayPercent(new BigDecimal("25.00"));
        insurance.setCoPayFixed(BigDecimal.ZERO);
        insurance.setRequiresAuthorization(true);
        insurance.setPortalUrl("https://portal.amil.com.br");
        insurance.setWebserviceUrl("https://api.amil.com.br/tiss");
        return insurance;
    }

    /**
     * Insurance that doesn't require authorization
     */
    public static Insurance withoutAuthorizationRequirement() {
        Insurance insurance = unimed();
        insurance.setRequiresAuthorization(false);
        return insurance;
    }

    /**
     * Simple consultation authorization request
     */
    public static AuthorizationRequest consultationRequest() {
        AuthorizationRequest request = new AuthorizationRequest();
        request.setAuthorizationNumber("AUTH" + System.currentTimeMillis());
        request.setPatientId("PAT123");
        request.setInsuranceId("UNIMED");
        request.setProcedureCodes(Arrays.asList("10101012")); // TUSS consultation
        request.setCidCodes(Arrays.asList("Z00.0"));
        request.setRequestDate(LocalDate.now());
        request.setProcedureDate(LocalDate.now().plusDays(7));
        request.setRequestingPhysician("Dr. João Silva - CRM 123456");
        request.setStatus("PENDING");
        request.setEstimatedCost(new BigDecimal("300.00"));
        return request;
    }

    /**
     * Surgery authorization request
     */
    public static AuthorizationRequest surgeryRequest() {
        AuthorizationRequest request = new AuthorizationRequest();
        request.setAuthorizationNumber("AUTH_SURG_" + System.currentTimeMillis());
        request.setPatientId("PAT456");
        request.setInsuranceId("UNIMED");
        request.setProcedureCodes(Arrays.asList(
            "31001192", // Cholecystectomy
            "20104049", // Anesthesia
            "70000014"  // Operating room
        ));
        request.setCidCodes(Arrays.asList("K80.2")); // Cholelithiasis
        request.setRequestDate(LocalDate.now());
        request.setProcedureDate(LocalDate.now().plusDays(15));
        request.setRequestingPhysician("Dr. Maria Santos - CRM 789012");
        request.setStatus("PENDING");
        request.setEstimatedCost(new BigDecimal("15000.00"));
        return request;
    }

    /**
     * Approved authorization
     */
    public static AuthorizationRequest approvedAuthorization() {
        AuthorizationRequest request = consultationRequest();
        request.setStatus("APPROVED");
        request.setAuthorizationNumber("AUTH_APPROVED_123456");
        return request;
    }

    /**
     * Denied authorization
     */
    public static AuthorizationRequest deniedAuthorization() {
        AuthorizationRequest request = consultationRequest();
        request.setStatus("DENIED");
        request.setDenialReason("Carência não cumprida");
        return request;
    }

    /**
     * Partial authorization
     */
    public static AuthorizationRequest partialAuthorization() {
        AuthorizationRequest request = surgeryRequest();
        request.setStatus("PARTIAL");
        request.setDenialReason("Aprovado apenas procedimento principal");
        return request;
    }

    /**
     * Authorization with timeout
     */
    public static AuthorizationRequest timedOutAuthorization() {
        AuthorizationRequest request = consultationRequest();
        request.setRequestDate(LocalDate.now().minusDays(3));
        request.setStatus("PENDING");
        return request;
    }
}
