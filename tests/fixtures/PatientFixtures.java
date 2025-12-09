package com.hospital.fixtures;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Test fixtures for Patient entities.
 * Provides consistent test data for patient-related scenarios.
 */
public class PatientFixtures {

    public static class Patient {
        private String id;
        private String cpf;
        private String name;
        private LocalDate birthDate;
        private String email;
        private String phone;
        private String insuranceId;
        private String insurancePlanCode;
        private String addressStreet;
        private String addressCity;
        private String addressState;
        private String addressZip;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getCpf() { return cpf; }
        public void setCpf(String cpf) { this.cpf = cpf; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public LocalDate getBirthDate() { return birthDate; }
        public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getInsuranceId() { return insuranceId; }
        public void setInsuranceId(String insuranceId) { this.insuranceId = insuranceId; }
        public String getInsurancePlanCode() { return insurancePlanCode; }
        public void setInsurancePlanCode(String insurancePlanCode) { this.insurancePlanCode = insurancePlanCode; }
        public String getAddressStreet() { return addressStreet; }
        public void setAddressStreet(String addressStreet) { this.addressStreet = addressStreet; }
        public String getAddressCity() { return addressCity; }
        public void setAddressCity(String addressCity) { this.addressCity = addressCity; }
        public String getAddressState() { return addressState; }
        public void setAddressState(String addressState) { this.addressState = addressState; }
        public String getAddressZip() { return addressZip; }
        public void setAddressZip(String addressZip) { this.addressZip = addressZip; }
    }

    /**
     * Default patient with all fields populated
     */
    public static Patient defaultPatient() {
        Patient patient = new Patient();
        patient.setId("PAT" + UUID.randomUUID().toString().substring(0, 8));
        patient.setCpf("12345678900");
        patient.setName("João Silva");
        patient.setBirthDate(LocalDate.of(1985, 5, 15));
        patient.setEmail("joao.silva@email.com");
        patient.setPhone("11987654321");
        patient.setAddressStreet("Rua das Flores, 123");
        patient.setAddressCity("São Paulo");
        patient.setAddressState("SP");
        patient.setAddressZip("01234-567");
        return patient;
    }

    /**
     * Patient with insurance
     */
    public static Patient withInsurance(String insuranceName) {
        Patient patient = defaultPatient();
        patient.setInsuranceId(insuranceName);
        patient.setInsurancePlanCode(insuranceName + "_PREMIUM_001");
        return patient;
    }

    /**
     * Patient without insurance (particular)
     */
    public static Patient withoutInsurance() {
        Patient patient = defaultPatient();
        patient.setInsuranceId(null);
        patient.setInsurancePlanCode(null);
        return patient;
    }

    /**
     * Emergency patient
     */
    public static Patient emergencyPatient() {
        Patient patient = new Patient();
        patient.setId("PAT_EMERGENCY_" + UUID.randomUUID().toString().substring(0, 8));
        patient.setCpf("98765432100");
        patient.setName("Maria Santos");
        patient.setBirthDate(LocalDate.of(1990, 3, 20));
        patient.setPhone("11912345678");
        return patient;
    }

    /**
     * Pediatric patient
     */
    public static Patient pediatricPatient() {
        Patient patient = defaultPatient();
        patient.setName("Pedro Silva");
        patient.setBirthDate(LocalDate.now().minusYears(5));
        return patient;
    }

    /**
     * Elderly patient
     */
    public static Patient elderlyPatient() {
        Patient patient = defaultPatient();
        patient.setName("Antonio Oliveira");
        patient.setBirthDate(LocalDate.of(1940, 1, 10));
        return patient;
    }

    /**
     * Patient with invalid CPF
     */
    public static Patient withInvalidCPF() {
        Patient patient = defaultPatient();
        patient.setCpf("00000000000");
        return patient;
    }

    /**
     * Patient with minimal data
     */
    public static Patient minimalPatient() {
        Patient patient = new Patient();
        patient.setName("Test Patient");
        patient.setCpf("11111111111");
        return patient;
    }

    /**
     * Patient with maximum length fields
     */
    public static Patient withMaxLengthFields() {
        Patient patient = defaultPatient();
        patient.setName("A".repeat(255));
        patient.setEmail("a".repeat(240) + "@test.com");
        patient.setAddressStreet("B".repeat(255));
        return patient;
    }

    /**
     * Multiple patients for batch testing
     */
    public static Patient[] multiplePatientsForBatchTesting(int count) {
        Patient[] patients = new Patient[count];
        for (int i = 0; i < count; i++) {
            patients[i] = defaultPatient();
            patients[i].setName("Patient " + i);
            patients[i].setCpf(String.format("%011d", i));
        }
        return patients;
    }
}
