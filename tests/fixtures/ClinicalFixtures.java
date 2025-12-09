package com.hospital.fixtures;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Test fixtures for Clinical data entities.
 */
public class ClinicalFixtures {

    public static class ClinicalData {
        private String accountId;
        private String patientId;
        private LocalDateTime admissionDate;
        private LocalDateTime dischargeDate;
        private String admissionType;
        private List<String> cidCodes;
        private List<Procedure> procedures;
        private List<MaterialUsage> materials;
        private List<MedicationAdministration> medications;
        private String dischargeSummary;
        private String dischargeType;

        // Getters and setters
        public String getAccountId() { return accountId; }
        public void setAccountId(String accountId) { this.accountId = accountId; }
        public String getPatientId() { return patientId; }
        public void setPatientId(String patientId) { this.patientId = patientId; }
        public LocalDateTime getAdmissionDate() { return admissionDate; }
        public void setAdmissionDate(LocalDateTime admissionDate) { this.admissionDate = admissionDate; }
        public LocalDateTime getDischargeDate() { return dischargeDate; }
        public void setDischargeDate(LocalDateTime dischargeDate) { this.dischargeDate = dischargeDate; }
        public String getAdmissionType() { return admissionType; }
        public void setAdmissionType(String admissionType) { this.admissionType = admissionType; }
        public List<String> getCidCodes() { return cidCodes; }
        public void setCidCodes(List<String> cidCodes) { this.cidCodes = cidCodes; }
        public List<Procedure> getProcedures() { return procedures; }
        public void setProcedures(List<Procedure> procedures) { this.procedures = procedures; }
        public List<MaterialUsage> getMaterials() { return materials; }
        public void setMaterials(List<MaterialUsage> materials) { this.materials = materials; }
        public List<MedicationAdministration> getMedications() { return medications; }
        public void setMedications(List<MedicationAdministration> medications) { this.medications = medications; }
        public String getDischargeSummary() { return dischargeSummary; }
        public void setDischargeSummary(String dischargeSummary) { this.dischargeSummary = dischargeSummary; }
        public String getDischargeType() { return dischargeType; }
        public void setDischargeType(String dischargeType) { this.dischargeType = dischargeType; }
    }

    public static class Procedure {
        private String tusscode;
        private String description;
        private LocalDateTime performedDate;
        private String performingPhysician;
        private BigDecimal quantity;
        private BigDecimal unitPrice;

        // Getters and setters
        public String getTusscode() { return tusscode; }
        public void setTusscode(String tusscode) { this.tusscode = tusscode; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public LocalDateTime getPerformedDate() { return performedDate; }
        public void setPerformedDate(LocalDateTime performedDate) { this.performedDate = performedDate; }
        public String getPerformingPhysician() { return performingPhysician; }
        public void setPerformingPhysician(String performingPhysician) { this.performingPhysician = performingPhysician; }
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    }

    public static class MaterialUsage {
        private String code;
        private String description;
        private BigDecimal quantity;
        private String unit;
        private BigDecimal unitPrice;
        private LocalDateTime usageDate;
        private String rfidTag;

        // Getters and setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public LocalDateTime getUsageDate() { return usageDate; }
        public void setUsageDate(LocalDateTime usageDate) { this.usageDate = usageDate; }
        public String getRfidTag() { return rfidTag; }
        public void setRfidTag(String rfidTag) { this.rfidTag = rfidTag; }
    }

    public static class MedicationAdministration {
        private String medicationCode;
        private String medicationName;
        private String dosage;
        private String route;
        private LocalDateTime administrationTime;
        private String administeredBy;

        // Getters and setters
        public String getMedicationCode() { return medicationCode; }
        public void setMedicationCode(String medicationCode) { this.medicationCode = medicationCode; }
        public String getMedicationName() { return medicationName; }
        public void setMedicationName(String medicationName) { this.medicationName = medicationName; }
        public String getDosage() { return dosage; }
        public void setDosage(String dosage) { this.dosage = dosage; }
        public String getRoute() { return route; }
        public void setRoute(String route) { this.route = route; }
        public LocalDateTime getAdministrationTime() { return administrationTime; }
        public void setAdministrationTime(LocalDateTime administrationTime) { this.administrationTime = administrationTime; }
        public String getAdministeredBy() { return administeredBy; }
        public void setAdministeredBy(String administeredBy) { this.administeredBy = administeredBy; }
    }

    /**
     * Simple consultation
     */
    public static ClinicalData simpleConsultation() {
        ClinicalData data = new ClinicalData();
        data.setAccountId("ACC" + System.currentTimeMillis());
        data.setPatientId("PAT123");
        data.setAdmissionDate(LocalDateTime.now().minusHours(2));
        data.setDischargeDate(LocalDateTime.now());
        data.setAdmissionType("OUTPATIENT");
        data.setCidCodes(Arrays.asList("Z00.0"));

        Procedure consultation = new Procedure();
        consultation.setTusscode("10101012");
        consultation.setDescription("Consulta médica");
        consultation.setPerformedDate(LocalDateTime.now().minusHours(1));
        consultation.setPerformingPhysician("Dr. João Silva");
        consultation.setQuantity(new BigDecimal("1"));
        consultation.setUnitPrice(new BigDecimal("300.00"));

        data.setProcedures(Arrays.asList(consultation));
        data.setMaterials(new ArrayList<>());
        data.setMedications(new ArrayList<>());
        data.setDischargeSummary("Patient consulted for routine checkup. No abnormalities found.");
        data.setDischargeType("REGULAR");

        return data;
    }

    /**
     * Surgical procedure
     */
    public static ClinicalData surgicalProcedure() {
        ClinicalData data = new ClinicalData();
        data.setAccountId("ACC_SURG_" + System.currentTimeMillis());
        data.setPatientId("PAT456");
        data.setAdmissionDate(LocalDateTime.now().minusDays(2));
        data.setDischargeDate(LocalDateTime.now());
        data.setAdmissionType("INPATIENT");
        data.setCidCodes(Arrays.asList("K80.2")); // Cholelithiasis

        // Main procedure
        Procedure surgery = new Procedure();
        surgery.setTusscode("31001192");
        surgery.setDescription("Colecistectomia videolaparoscópica");
        surgery.setPerformedDate(LocalDateTime.now().minusDays(1));
        surgery.setPerformingPhysician("Dr. Maria Santos");
        surgery.setQuantity(new BigDecimal("1"));
        surgery.setUnitPrice(new BigDecimal("8000.00"));

        // Anesthesia
        Procedure anesthesia = new Procedure();
        anesthesia.setTusscode("20104049");
        anesthesia.setDescription("Anestesia geral");
        anesthesia.setPerformedDate(LocalDateTime.now().minusDays(1));
        anesthesia.setPerformingPhysician("Dr. Carlos Anjos");
        anesthesia.setQuantity(new BigDecimal("1"));
        anesthesia.setUnitPrice(new BigDecimal("1500.00"));

        // Operating room
        Procedure operatingRoom = new Procedure();
        operatingRoom.setTusscode("70000014");
        operatingRoom.setDescription("Sala cirúrgica");
        operatingRoom.setPerformedDate(LocalDateTime.now().minusDays(1));
        operatingRoom.setPerformingPhysician("N/A");
        operatingRoom.setQuantity(new BigDecimal("4")); // 4 hours
        operatingRoom.setUnitPrice(new BigDecimal("500.00"));

        data.setProcedures(Arrays.asList(surgery, anesthesia, operatingRoom));

        // Materials
        MaterialUsage suture = new MaterialUsage();
        suture.setCode("MAT001");
        suture.setDescription("Fio de sutura absorvível");
        suture.setQuantity(new BigDecimal("5"));
        suture.setUnit("UN");
        suture.setUnitPrice(new BigDecimal("45.00"));
        suture.setUsageDate(LocalDateTime.now().minusDays(1));
        suture.setRfidTag("RFID123456");

        data.setMaterials(Arrays.asList(suture));

        // Medications
        MedicationAdministration antibiotic = new MedicationAdministration();
        antibiotic.setMedicationCode("MED001");
        antibiotic.setMedicationName("Ceftriaxona 1g");
        antibiotic.setDosage("1g");
        antibiotic.setRoute("IV");
        antibiotic.setAdministrationTime(LocalDateTime.now().minusDays(1));
        antibiotic.setAdministeredBy("Enf. Ana Costa");

        data.setMedications(Arrays.asList(antibiotic));

        data.setDischargeSummary("Patient underwent laparoscopic cholecystectomy. Procedure completed successfully. No complications. Patient stable and discharged.");
        data.setDischargeType("REGULAR");

        return data;
    }

    /**
     * Emergency admission
     */
    public static ClinicalData emergencyAdmission() {
        ClinicalData data = new ClinicalData();
        data.setAccountId("ACC_EMERG_" + System.currentTimeMillis());
        data.setPatientId("PAT_EMERG_789");
        data.setAdmissionDate(LocalDateTime.now().minusHours(6));
        data.setDischargeDate(null); // Still admitted
        data.setAdmissionType("EMERGENCY");
        data.setCidCodes(Arrays.asList("I21.9")); // Acute myocardial infarction

        Procedure emergencyCare = new Procedure();
        emergencyCare.setTusscode("30301010");
        emergencyCare.setDescription("Atendimento emergência cardiológica");
        emergencyCare.setPerformedDate(LocalDateTime.now().minusHours(5));
        emergencyCare.setPerformingPhysician("Dr. Pedro Cardio");
        emergencyCare.setQuantity(new BigDecimal("1"));
        emergencyCare.setUnitPrice(new BigDecimal("2500.00"));

        data.setProcedures(Arrays.asList(emergencyCare));
        data.setMaterials(new ArrayList<>());
        data.setMedications(new ArrayList<>());

        return data;
    }

    /**
     * ICU admission
     */
    public static ClinicalData icuAdmission() {
        ClinicalData data = surgicalProcedure();
        data.setAdmissionType("ICU");

        Procedure icuDaily = new Procedure();
        icuDaily.setTusscode("70000107");
        icuDaily.setDescription("Diária de UTI");
        icuDaily.setPerformedDate(LocalDateTime.now().minusDays(1));
        icuDaily.setPerformingPhysician("Equipe UTI");
        icuDaily.setQuantity(new BigDecimal("3")); // 3 days
        icuDaily.setUnitPrice(new BigDecimal("1500.00"));

        List<Procedure> procedures = new ArrayList<>(data.getProcedures());
        procedures.add(icuDaily);
        data.setProcedures(procedures);

        return data;
    }

    /**
     * Incomplete documentation
     */
    public static ClinicalData incompleteDocumentation() {
        ClinicalData data = simpleConsultation();
        data.setDischargeSummary(null);
        data.setCidCodes(new ArrayList<>()); // Missing CID
        return data;
    }

    /**
     * High complexity case
     */
    public static ClinicalData highComplexityCase() {
        ClinicalData data = surgicalProcedure();
        data.setCidCodes(Arrays.asList("C18.9", "E11.9")); // Cancer + Diabetes

        // Add more procedures
        Procedure imaging = new Procedure();
        imaging.setTusscode("40301010");
        imaging.setDescription("Tomografia computadorizada");
        imaging.setPerformedDate(LocalDateTime.now().minusDays(1));
        imaging.setPerformingPhysician("Dr. Radiologia");
        imaging.setQuantity(new BigDecimal("1"));
        imaging.setUnitPrice(new BigDecimal("800.00"));

        List<Procedure> procedures = new ArrayList<>(data.getProcedures());
        procedures.add(imaging);
        data.setProcedures(procedures);

        return data;
    }
}
