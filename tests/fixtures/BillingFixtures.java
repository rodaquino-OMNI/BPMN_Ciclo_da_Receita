package com.hospital.fixtures;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Test fixtures for Billing and Financial entities.
 */
public class BillingFixtures {

    public static class BillingData {
        private String accountId;
        private String patientId;
        private String insuranceId;
        private LocalDate billingDate;
        private LocalDate submissionDate;
        private String status;
        private BigDecimal totalAmount;
        private BigDecimal coPayAmount;
        private BigDecimal insuranceAmount;
        private BigDecimal paidAmount;
        private List<BillingItem> items;
        private String tissGuideNumber;
        private String protocolNumber;
        private List<Denial> denials;

        // Getters and setters
        public String getAccountId() { return accountId; }
        public void setAccountId(String accountId) { this.accountId = accountId; }
        public String getPatientId() { return patientId; }
        public void setPatientId(String patientId) { this.patientId = patientId; }
        public String getInsuranceId() { return insuranceId; }
        public void setInsuranceId(String insuranceId) { this.insuranceId = insuranceId; }
        public LocalDate getBillingDate() { return billingDate; }
        public void setBillingDate(LocalDate billingDate) { this.billingDate = billingDate; }
        public LocalDate getSubmissionDate() { return submissionDate; }
        public void setSubmissionDate(LocalDate submissionDate) { this.submissionDate = submissionDate; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public BigDecimal getCoPayAmount() { return coPayAmount; }
        public void setCoPayAmount(BigDecimal coPayAmount) { this.coPayAmount = coPayAmount; }
        public BigDecimal getInsuranceAmount() { return insuranceAmount; }
        public void setInsuranceAmount(BigDecimal insuranceAmount) { this.insuranceAmount = insuranceAmount; }
        public BigDecimal getPaidAmount() { return paidAmount; }
        public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
        public List<BillingItem> getItems() { return items; }
        public void setItems(List<BillingItem> items) { this.items = items; }
        public String getTissGuideNumber() { return tissGuideNumber; }
        public void setTissGuideNumber(String tissGuideNumber) { this.tissGuideNumber = tissGuideNumber; }
        public String getProtocolNumber() { return protocolNumber; }
        public void setProtocolNumber(String protocolNumber) { this.protocolNumber = protocolNumber; }
        public List<Denial> getDenials() { return denials; }
        public void setDenials(List<Denial> denials) { this.denials = denials; }
    }

    public static class BillingItem {
        private String itemId;
        private String type; // PROCEDURE, MATERIAL, MEDICATION, DAILY
        private String code;
        private String description;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private LocalDate serviceDate;

        // Getters and setters
        public String getItemId() { return itemId; }
        public void setItemId(String itemId) { this.itemId = itemId; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public BigDecimal getTotalPrice() { return totalPrice; }
        public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
        public LocalDate getServiceDate() { return serviceDate; }
        public void setServiceDate(LocalDate serviceDate) { this.serviceDate = serviceDate; }
    }

    public static class Denial {
        private String denialId;
        private String accountId;
        private String denialType;
        private String denialCode;
        private String denialReason;
        private BigDecimal deniedAmount;
        private LocalDate denialDate;
        private String status; // PENDING, APPEALED, RECOVERED, LOST
        private String appealText;
        private LocalDate appealDate;
        private BigDecimal recoveredAmount;

        // Getters and setters
        public String getDenialId() { return denialId; }
        public void setDenialId(String denialId) { this.denialId = denialId; }
        public String getAccountId() { return accountId; }
        public void setAccountId(String accountId) { this.accountId = accountId; }
        public String getDenialType() { return denialType; }
        public void setDenialType(String denialType) { this.denialType = denialType; }
        public String getDenialCode() { return denialCode; }
        public void setDenialCode(String denialCode) { this.denialCode = denialCode; }
        public String getDenialReason() { return denialReason; }
        public void setDenialReason(String denialReason) { this.denialReason = denialReason; }
        public BigDecimal getDeniedAmount() { return deniedAmount; }
        public void setDeniedAmount(BigDecimal deniedAmount) { this.deniedAmount = deniedAmount; }
        public LocalDate getDenialDate() { return denialDate; }
        public void setDenialDate(LocalDate denialDate) { this.denialDate = denialDate; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getAppealText() { return appealText; }
        public void setAppealText(String appealText) { this.appealText = appealText; }
        public LocalDate getAppealDate() { return appealDate; }
        public void setAppealDate(LocalDate appealDate) { this.appealDate = appealDate; }
        public BigDecimal getRecoveredAmount() { return recoveredAmount; }
        public void setRecoveredAmount(BigDecimal recoveredAmount) { this.recoveredAmount = recoveredAmount; }
    }

    public static class Payment {
        private String paymentId;
        private String accountId;
        private BigDecimal amount;
        private LocalDate paymentDate;
        private String paymentMethod;
        private String transactionId;
        private String status;

        // Getters and setters
        public String getPaymentId() { return paymentId; }
        public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
        public String getAccountId() { return accountId; }
        public void setAccountId(String accountId) { this.accountId = accountId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public LocalDate getPaymentDate() { return paymentDate; }
        public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    /**
     * Completed account ready for billing
     */
    public static BillingData completedAccount() {
        BillingData billing = new BillingData();
        billing.setAccountId("ACC123");
        billing.setPatientId("PAT123");
        billing.setInsuranceId("UNIMED");
        billing.setBillingDate(LocalDate.now());
        billing.setStatus("READY_TO_SUBMIT");

        // Add billing items
        BillingItem consultation = new BillingItem();
        consultation.setItemId("ITEM001");
        consultation.setType("PROCEDURE");
        consultation.setCode("10101012");
        consultation.setDescription("Consulta médica");
        consultation.setQuantity(new BigDecimal("1"));
        consultation.setUnitPrice(new BigDecimal("300.00"));
        consultation.setTotalPrice(new BigDecimal("300.00"));
        consultation.setServiceDate(LocalDate.now().minusDays(1));

        billing.setItems(Arrays.asList(consultation));
        billing.setTotalAmount(new BigDecimal("300.00"));
        billing.setCoPayAmount(new BigDecimal("60.00")); // 20% copay
        billing.setInsuranceAmount(new BigDecimal("240.00"));
        billing.setDenials(new ArrayList<>());

        return billing;
    }

    /**
     * Submitted account with protocol
     */
    public static BillingData submittedAccount() {
        BillingData billing = completedAccount();
        billing.setStatus("SUBMITTED");
        billing.setSubmissionDate(LocalDate.now());
        billing.setTissGuideNumber("TISS" + System.currentTimeMillis());
        billing.setProtocolNumber("PROT" + System.currentTimeMillis());
        return billing;
    }

    /**
     * Account with denials
     */
    public static BillingData accountWithDenials() {
        BillingData billing = submittedAccount();
        billing.setStatus("DENIED");

        Denial denial = new Denial();
        denial.setDenialId("DEN001");
        denial.setAccountId(billing.getAccountId());
        denial.setDenialType("TECHNICAL");
        denial.setDenialCode("001");
        denial.setDenialReason("Documentação incompleta");
        denial.setDeniedAmount(new BigDecimal("300.00"));
        denial.setDenialDate(LocalDate.now());
        denial.setStatus("PENDING");

        billing.setDenials(Arrays.asList(denial));

        return billing;
    }

    /**
     * Account with successful appeal
     */
    public static BillingData accountWithSuccessfulAppeal() {
        BillingData billing = accountWithDenials();

        Denial denial = billing.getDenials().get(0);
        denial.setStatus("RECOVERED");
        denial.setAppealText("Documentação complementar enviada conforme solicitado");
        denial.setAppealDate(LocalDate.now().minusDays(5));
        denial.setRecoveredAmount(denial.getDeniedAmount());

        billing.setStatus("APPROVED");
        return billing;
    }

    /**
     * Full payment received
     */
    public static Payment fullPayment() {
        Payment payment = new Payment();
        payment.setPaymentId("PAY" + System.currentTimeMillis());
        payment.setAccountId("ACC123");
        payment.setAmount(new BigDecimal("300.00"));
        payment.setPaymentDate(LocalDate.now());
        payment.setPaymentMethod("BANK_TRANSFER");
        payment.setTransactionId("TRX" + System.currentTimeMillis());
        payment.setStatus("COMPLETED");
        return payment;
    }

    /**
     * Partial payment
     */
    public static Payment partialPayment() {
        Payment payment = fullPayment();
        payment.setAmount(new BigDecimal("150.00"));
        return payment;
    }

    /**
     * Large surgical account
     */
    public static BillingData surgicalAccount() {
        BillingData billing = new BillingData();
        billing.setAccountId("ACC_SURG_456");
        billing.setPatientId("PAT456");
        billing.setInsuranceId("UNIMED");
        billing.setBillingDate(LocalDate.now());
        billing.setStatus("READY_TO_SUBMIT");

        List<BillingItem> items = new ArrayList<>();

        // Surgery
        BillingItem surgery = new BillingItem();
        surgery.setItemId("ITEM001");
        surgery.setType("PROCEDURE");
        surgery.setCode("31001192");
        surgery.setDescription("Colecistectomia");
        surgery.setQuantity(new BigDecimal("1"));
        surgery.setUnitPrice(new BigDecimal("8000.00"));
        surgery.setTotalPrice(new BigDecimal("8000.00"));
        surgery.setServiceDate(LocalDate.now().minusDays(2));
        items.add(surgery);

        // Anesthesia
        BillingItem anesthesia = new BillingItem();
        anesthesia.setItemId("ITEM002");
        anesthesia.setType("PROCEDURE");
        anesthesia.setCode("20104049");
        anesthesia.setDescription("Anestesia geral");
        anesthesia.setQuantity(new BigDecimal("1"));
        anesthesia.setUnitPrice(new BigDecimal("1500.00"));
        anesthesia.setTotalPrice(new BigDecimal("1500.00"));
        anesthesia.setServiceDate(LocalDate.now().minusDays(2));
        items.add(anesthesia);

        // Operating room
        BillingItem room = new BillingItem();
        room.setItemId("ITEM003");
        room.setType("DAILY");
        room.setCode("70000014");
        room.setDescription("Sala cirúrgica");
        room.setQuantity(new BigDecimal("4"));
        room.setUnitPrice(new BigDecimal("500.00"));
        room.setTotalPrice(new BigDecimal("2000.00"));
        room.setServiceDate(LocalDate.now().minusDays(2));
        items.add(room);

        // Materials
        BillingItem material = new BillingItem();
        material.setItemId("ITEM004");
        material.setType("MATERIAL");
        material.setCode("MAT001");
        material.setDescription("Materiais cirúrgicos");
        material.setQuantity(new BigDecimal("1"));
        material.setUnitPrice(new BigDecimal("500.00"));
        material.setTotalPrice(new BigDecimal("500.00"));
        material.setServiceDate(LocalDate.now().minusDays(2));
        items.add(material);

        billing.setItems(items);
        billing.setTotalAmount(new BigDecimal("12000.00"));
        billing.setCoPayAmount(new BigDecimal("2400.00")); // 20% copay
        billing.setInsuranceAmount(new BigDecimal("9600.00"));
        billing.setDenials(new ArrayList<>());

        return billing;
    }

    /**
     * Invalid billing data
     */
    public static BillingData invalidBillingData() {
        BillingData billing = completedAccount();
        billing.setTissGuideNumber(null);
        billing.setItems(new ArrayList<>()); // No items
        billing.setTotalAmount(BigDecimal.ZERO);
        return billing;
    }
}
