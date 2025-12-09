package com.hospital.delegates.collection;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delegate to send payment reminder to patient
 */
public class SendPaymentReminderDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendPaymentReminderDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        LOGGER.info("Sending payment reminder for process instance: {}",
            execution.getProcessInstanceId());

        try {
            // Get input variables
            String patientId = (String) execution.getVariable("patientId");
            Double patientBalance = (Double) execution.getVariable("patientBalance");
            String contactMethod = (String) execution.getVariable("preferredContactMethod");
            Integer reminderCount = (Integer) execution.getVariable("reminderCount");

            if (reminderCount == null) {
                reminderCount = 0;
            }

            LOGGER.debug("Sending reminder - Patient: {}, Balance: {}, Method: {}, Count: {}",
                patientId, patientBalance, contactMethod, reminderCount);

            // Send reminder
            ReminderResult result = sendReminder(patientId, patientBalance, contactMethod, reminderCount);

            // Increment reminder count
            reminderCount++;

            // Set output variables
            execution.setVariable("reminderSent", result.sent);
            execution.setVariable("reminderDate", result.sentDate);
            execution.setVariable("reminderMethod", result.method);
            execution.setVariable("reminderCount", reminderCount);
            execution.setVariable("nextReminderDate", result.nextReminderDate);

            LOGGER.info("Payment reminder sent - Sent: {}, Method: {}, Total count: {}",
                result.sent, result.method, reminderCount);

        } catch (Exception e) {
            LOGGER.error("Error sending payment reminder: {}", e.getMessage(), e);
            execution.setVariable("reminderError", e.getMessage());
            execution.setVariable("reminderSent", false);
            throw e;
        }
    }

    private ReminderResult sendReminder(String patientId, Double balance, String method, Integer count) {
        // Simulated reminder sending
        // TODO: Replace with actual notification system (email, SMS, letter)
        ReminderResult result = new ReminderResult();
        result.sent = true;
        result.method = method != null ? method : "EMAIL";
        result.sentDate = java.time.LocalDateTime.now().toString();

        // Schedule next reminder based on count
        int daysUntilNext = count < 2 ? 7 : 14;
        result.nextReminderDate = java.time.LocalDate.now().plusDays(daysUntilNext).toString();

        return result;
    }

    private static class ReminderResult {
        Boolean sent;
        String method;
        String sentDate;
        String nextReminderDate;
    }
}
